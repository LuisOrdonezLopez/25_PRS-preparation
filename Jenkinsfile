pipeline {
    // Usa el nodo disponible (en tu Docker aparece como "Jenkins").
    // Si creas un agente separado con label linux, puedes volver a usar:
    // agent { label 'linux' }
    agent any

    options {
        skipDefaultCheckout(true)
        disableConcurrentBuilds()
        timestamps()
        timeout(time: 45, unit: 'MINUTES')
    }

    parameters {
        password(name: 'JWT_TOKEN', defaultValue: '', description: 'Opcional: token JWT para probar datos protegidos con JMeter.')
        booleanParam(name: 'ENFORCE_SONAR_GATE', defaultValue: false, description: 'Opcional: evalúa el Quality Gate. Déjalo en false si no tienes webhook de SonarCloud.')
        string(name: 'SELENIUM_REMOTE_URL', defaultValue: '', trim: true, description: 'URL opcional de Selenium Grid, por ejemplo http://selenium-chrome:4444/wd/hub. Vacío usa Chrome local.')
        string(name: 'SELENIUM_BASE_URL', defaultValue: 'http://jenkins:8097', trim: true, description: 'URL de la aplicación visible desde el navegador Selenium. En Docker remoto usa http://jenkins:8097.')
    }

    environment {
        // El repositorio 25_PRS-preparation de GitHub tiene pom.xml en su raíz.
        // El valor por defecto evita que Jenkins construya una ruta "null".
        PROJECT_DIR = '.'
        SLACK_CHANNEL = '#gi08-pipeline'
        SONAR_PROJECT_KEY = 'gi08-preparationservice'
        SONAR_ORGANIZATION = 'luisordonezlopez'
        SONAR_CLOUD_URL = 'https://sonarcloud.io'
        APP_PORT = '8097'
        APP_BASE_URL = 'http://127.0.0.1:8097'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    // También permite usar este Jenkinsfile desde un repositorio
                    // padre que contenga la carpeta 25_PRS-preparation.
                    if (!fileExists('pom.xml')) {
                        if (fileExists('25_PRS-preparation/pom.xml')) {
                            env.PROJECT_DIR = '25_PRS-preparation'
                        } else {
                            error 'No se encontró pom.xml en la raíz ni en 25_PRS-preparation/'
                        }
                    }
                    echo "Proyecto Maven detectado en: ${env.PROJECT_DIR}"
                }
            }
        }

        stage('Compilar y pruebas unitarias') {
            steps {
                dir("${env.PROJECT_DIR}") {
                    sh 'mvn -B clean verify -DskipITs=true'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'
                }
            }
        }

        stage('Análisis SonarQube') {
            steps {
                dir("${env.PROJECT_DIR}") {
                    // La instalación Jenkins puede conservar el nombre "SonarQube",
                    // pero el análisis apunta explícitamente a SonarCloud.
                    withSonarQubeEnv('SonarQube') {
                        // La credencial nunca se escribe en el repositorio ni en la línea
                        // de comandos; Jenkins la enmascara en la consola.
                        withCredentials([string(credentialsId: 'sonarcloud-token', variable: 'SONAR_TOKEN')]) {
                            sh 'mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751:sonar -Dsonar.host.url="${SONAR_CLOUD_URL}" -Dsonar.organization="${SONAR_ORGANIZATION}" -Dsonar.projectKey="${SONAR_PROJECT_KEY}"'
                        }
                    }
                }
            }
        }

        stage('Quality Gate SonarQube') {
            when {
                expression { return params.ENFORCE_SONAR_GATE }
            }
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Pruebas funcionales Selenium') {
            steps {
                dir("${env.PROJECT_DIR}") {
                    withEnv([
                        "SELENIUM_REMOTE_URL=${params.SELENIUM_REMOTE_URL ?: ''}",
                        "SELENIUM_BASE_URL=${params.SELENIUM_BASE_URL ?: 'http://jenkins:8097'}"
                    ]) {
                        sh '''
                            set -eu
                            mkdir -p reports/functional
                            nohup mvn -B spring-boot:run -DskipTests -Dspring-boot.run.profiles=pipeline \
                                > reports/functional/application.log 2>&1 &
                            app_pid=$!
                            echo "$app_pid" > reports/functional/application.pid
                            trap 'kill "$app_pid" 2>/dev/null || true' EXIT

                            ready=false
                            for attempt in $(seq 1 30); do
                                if curl --silent --fail "${APP_BASE_URL}/swagger-ui/index.html" > /dev/null; then
                                    ready=true
                                    break
                                fi
                                sleep 2
                            done
                            if [ "$ready" != true ]; then
                                cat reports/functional/application.log
                                exit 1
                            fi

                            echo "Selenium navegará a: ${SELENIUM_BASE_URL}"
                            mvn -B -Pselenium verify -DskipITs=false
                        '''
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/TEST-*.xml'
                    archiveArtifacts allowEmptyArchive: true, artifacts: "${env.PROJECT_DIR}/reports/functional/**"
                }
            }
        }

        stage('Pruebas de carga JMeter: Verde-Amarillo-Rojo') {
            steps {
                dir("${env.PROJECT_DIR}") {
                    withEnv(["JMETER_JWT_TOKEN=${params.JWT_TOKEN}"]) {
                        sh '''
                            set -eu
                            mkdir -p reports/jmeter
                            nohup mvn -B spring-boot:run -DskipTests -Dspring-boot.run.profiles=pipeline \
                                > reports/jmeter/application.log 2>&1 &
                            app_pid=$!
                            trap 'kill "$app_pid" 2>/dev/null || true' EXIT

                            ready=false
                            for attempt in $(seq 1 30); do
                                if curl --silent --fail "${APP_BASE_URL}/api-docs" > /dev/null; then
                                    ready=true
                                    break
                                fi
                                sleep 2
                            done
                            if [ "$ready" != true ]; then
                                cat reports/jmeter/application.log
                                exit 1
                            fi

                            jmeter -n -t performance/preparation-load.jmx \
                                -JtargetHost=127.0.0.1 -JtargetPort="${APP_PORT}" \
                                -JjwtToken="${JMETER_JWT_TOKEN}" \
                                -l reports/jmeter/results.jtl \
                                -e -o reports/jmeter/html
                        '''
                    }
                }
            }
            post {
                always {
                    archiveArtifacts allowEmptyArchive: true, artifacts: "${env.PROJECT_DIR}/reports/jmeter/**"
                }
            }
        }
    }

    post {
        always {
            script {
                def result = currentBuild.currentResult ?: 'SUCCESS'
                def color = result == 'SUCCESS' ? 'good' : (result == 'UNSTABLE' ? 'warning' : 'danger')
                def message = "GI08 | preparation-service | Build #${env.BUILD_NUMBER} | ${result} | ${env.BUILD_URL}"
                try {
                    slackSend(channel: env.SLACK_CHANNEL, color: color, message: message)
                } catch (err) {
                    // Una falla de Slack no debe ocultar el resultado técnico del pipeline.
                    echo "No se pudo enviar la notificación a Slack: ${err.getMessage()}"
                }
            }
        }
    }
}
