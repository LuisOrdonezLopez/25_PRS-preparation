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
        booleanParam(name: 'ENFORCE_SONAR_GATE', defaultValue: false, description: 'Falla el pipeline si SonarQube rechaza el Quality Gate. Requiere webhook Jenkins.')
    }

    environment {
        PROJECT_DIR = ''
        SLACK_CHANNEL = '#gi08-pipeline'
        SONAR_PROJECT_KEY = 'gi08-preparationservice'
        APP_PORT = '8097'
        SELENIUM_BASE_URL = 'http://127.0.0.1:8097'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    // Permite usar este Jenkinsfile tanto si el repo es la carpeta
                    // 25_PRS-preparation como si el repo contiene esa carpeta.
                    env.PROJECT_DIR = fileExists('pom.xml') ? '.' : '25_PRS-preparation'
                }
            }
        }

        stage('Compilar y pruebas unitarias') {
            steps {
                dir("${env.PROJECT_DIR}") {
                    sh 'mvn -B clean verify'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: "${env.PROJECT_DIR}/target/surefire-reports/*.xml"
                }
            }
        }

        stage('Análisis SonarQube') {
            steps {
                dir("${env.PROJECT_DIR}") {
                    withSonarQubeEnv('SonarQube') {
                        sh 'mvn -B sonar:sonar -Dsonar.projectKey="${SONAR_PROJECT_KEY}"'
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
                            if curl --silent --fail "${SELENIUM_BASE_URL}/swagger-ui/index.html" > /dev/null; then
                                ready=true
                                break
                            fi
                            sleep 2
                        done
                        if [ "$ready" != true ]; then
                            cat reports/functional/application.log
                            exit 1
                        fi

                        mvn -B -Pselenium verify
                    '''
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: "${env.PROJECT_DIR}/target/failsafe-reports/*.xml"
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
                                if curl --silent --fail "${SELENIUM_BASE_URL}/api-docs" > /dev/null; then
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
