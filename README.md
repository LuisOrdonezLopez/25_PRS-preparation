# GI08 / Sprint 08 — Pipeline CI/CD del `preparation-service`

Este directorio contiene una implementación reproducible de la actividad PSW:

- `Jenkinsfile`: compila, ejecuta pruebas unitarias, analiza con SonarQube, ejecuta Selenium, ejecuta JMeter y notifica a Slack.
- `src/test/.../PreparationServiceTest.java`: pruebas unitarias del caso de uso.
- `src/test/.../PreparationFunctionalSeleniumIT.java`: dos pruebas funcionales de navegador.
- `performance/preparation-load.jmx`: escenarios de carga Verde, Amarillo y Rojo.
- `application-pipeline.yml` y `schema-pipeline.sql`: perfil aislado para CI con H2; no usa la base real del proyecto.
- `sonar-project.properties`: configuración del análisis y cobertura.

## 1. Conceptos para el informe

Una prueba funcional verifica que una funcionalidad cumpla el comportamiento esperado desde la perspectiva del usuario o consumidor. Comprueba entradas, reglas, respuestas, permisos y flujos completos; no se limita a comprobar métodos internos.

Selenium es un conjunto de herramientas para automatizar navegadores reales. Permite abrir una URL, interactuar con elementos HTML, esperar cambios asíncronos y validar el resultado. En este proyecto se usa Selenium WebDriver con Chrome en modo headless.

Las dos pruebas implementadas son:

| Caso | Funcionalidad | Pasos | Resultado esperado |
|---|---|---|---|
| F-01 | Documentación de operaciones de preparaciones | Abrir `/swagger-ui/index.html`, esperar la interfaz y leer el contenido | La interfaz Swagger carga y contiene la palabra `preparation`. |
| F-02 | Seguridad de las sesiones de preparación | Abrir `/api/v1/preparations/request/1` sin token | El endpoint protegido responde HTTP `401 Unauthorized`. |

Después de ejecutar Jenkins, adjunta en tu informe una captura de la pestaña `Test Result` o de los reportes JUnit de Selenium y completa el “resultado obtenido” con el valor real mostrado.

## 2. Configuración previa de Jenkins

Jenkins no necesita que el código sea público si tiene credenciales para clonar el repositorio. Para una entrega académica puede usarse un repositorio público, pero nunca se deben publicar contraseñas.

Instala/configura en Jenkins:

1. Plugins: **Pipeline**, **Git**, **JUnit**, **SonarQube Scanner**, **Slack Notification** y **HTML Publisher** (este último es opcional para visualizar el informe HTML de JMeter).
2. Herramientas del agente Linux: JDK 17, Maven 3.9+, Google Chrome/Chromium, ChromeDriver administrado por Selenium Manager y Apache JMeter 5.6+ en el `PATH`.
3. En `Manage Jenkins > System > SonarQube servers`, registra un servidor con el nombre exacto `SonarQube`, su URL y un token de análisis.
4. En `Manage Jenkins > System > Slack`, configura el workspace y una credencial de tipo **Secret text** para el plugin Slack. Invita la aplicación/bot de Jenkins al canal `#gi08-pipeline`.
5. El agente debe tener `curl`. El pipeline usa el perfil H2 para poder levantar el microservicio sin depender de PostgreSQL, Keycloak ni los microservicios externos.

### Crear el job

En Jenkins crea un job de tipo **Pipeline > Pipeline script from SCM**, selecciona Git y apunta al repositorio.

- Si el repositorio contiene esta carpeta: usa `25_PRS-preparation/Jenkinsfile` como **Script Path**.
- Si `25_PRS-preparation` es la raíz del repositorio: usa `Jenkinsfile`.

El pipeline detecta ambos casos automáticamente. El canal ya está fijado en `#gi08-pipeline` en `Jenkinsfile`.

## 3. SonarQube

El stage `Análisis SonarQube` ejecuta `mvn sonar:sonar`, publica los binarios y usa `target/site/jacoco/jacoco.xml` para cobertura. Para que `Quality Gate` bloquee el build:

1. En SonarQube crea/configura el proyecto con la key `gi08-preparationservice`.
2. En `Administration > Configuration > Webhooks`, agrega `http://JENKINS_URL/sonarqube-webhook/`.
3. Ejecuta el job con el parámetro `ENFORCE_SONAR_GATE=true`.
4. Abre el proyecto en SonarQube y captura la pantalla con Quality Gate, Bugs, Vulnerabilities, Code Smells, Coverage y Duplications.

No inventes los valores del informe: copia los números reales de esa ejecución. La interpretación mínima es: Quality Gate indica si cumple las reglas; Bugs/Vulnerabilities representan defectos y riesgos; Code Smells indica deuda técnica; Coverage indica qué parte de las pruebas cubre el código; Duplications muestra código repetido.

## 4. Slack y Jenkins

El bloque `post { always { ... } }` envía una notificación al finalizar, incluso si una etapa falla. El mensaje contiene proyecto, número de build, estado y URL de Jenkins. Ejecuta `Build with Parameters`, espera el final y captura en Slack `#gi08-pipeline` el mensaje `GI08 | preparation-service | Build #...`.

Si Jenkins muestra que `slackSend` no existe, falta el plugin Slack Notification. Si el mensaje no llega, revisa la credencial global, el workspace, que el bot esté dentro del canal y que el nombre sea exactamente `#gi08-pipeline`.

## 5. Pruebas de carga con JMeter

El plan ejecuta los grupos de forma secuencial y usa el endpoint `GET /api/v1/preparations/request/{requestId}`. Los tres escenarios son:

| Escenario | Usuarios concurrentes | Ramp-up | Iteraciones | Peticiones previstas | Interpretación |
|---|---:|---:|---:|---:|---|
| Verde | 5 | 10 s | 10 | 50 | Carga normal esperada. |
| Amarillo | 25 | 20 s | 20 | 500 | Carga media; muestra degradación progresiva. |
| Rojo | 100 | 30 s | 20 | 2 000 | Estrés; identifica saturación, errores o aumento de latencia. |

Desde Jenkins se genera `reports/jmeter/results.jtl` y el informe HTML en `reports/jmeter/html/`. Para ejecutarlo manualmente:

```bash
jmeter -n -t performance/preparation-load.jmx \
  -JtargetHost=localhost -JtargetPort=8097 \
  -JrequestId=1 -JjwtToken="TU_TOKEN_JWT" \
  -l reports/jmeter/results.jtl \
  -e -o reports/jmeter/html
```

El token es opcional para comprobar el rechazo de usuarios anónimos (`401`). Para medir la consulta funcional con datos, usa un JWT válido de Keycloak con el rol `SECRETARIO` o `PARROCO` y un `requestId` existente. No escribas el token en el repositorio ni en una captura.

En el informe de JMeter registra por escenario: número de muestras, throughput, tiempo medio, mediana, percentil 90/95, mínimo, máximo, porcentaje de error y código HTTP. Interpreta Verde como línea base, Amarillo comparando el aumento de latencia/error contra Verde, y Rojo determinando si el servicio mantiene respuestas aceptables o se satura. Adjunta una captura del dashboard HTML o de la tabla Aggregate Report.

## 6. Ejecución local de Selenium

Con el servicio disponible en `http://localhost:8097`, Chrome instalado y `chromedriver` disponible, ejecuta:

```bash
mvn -B -Pselenium verify -Dselenium.baseUrl=http://localhost:8097
```

Jenkins levanta automáticamente el servicio con `spring-boot.run.profiles=pipeline`, espera Swagger y luego ejecuta estas dos pruebas. Si aparece `SessionNotCreatedException`, instala/actualiza Chrome en el agente o usa un Selenium Grid definiendo `SELENIUM_REMOTE_URL`.

## 7. Evidencias que faltan hacer en la infraestructura real

Desde este entorno no tengo acceso a tu instancia de Jenkins, servidor SonarQube, workspace de Slack ni a un navegador/JMeter instalado; por eso no puedo generar capturas auténticas ni afirmar métricas de una ejecución. Para completar la entrega:

1. Publica/sube el contenido de esta carpeta al repositorio que usará Jenkins.
2. Configura Jenkins, SonarQube y Slack según las secciones anteriores.
3. Ejecuta `Build with Parameters` y verifica que las etapas queden verdes.
4. Toma tres evidencias: notificación en Slack, dashboard de SonarQube y reporte HTML/tabla de JMeter; además conserva el resultado JUnit de Selenium.
5. En tu documento académico copia las tablas de escenarios y reemplaza los campos de resultados por los valores reales de esa ejecución.

## 8. Seguridad antes de publicar

La configuración productiva usa `R2DBC_URL`, `R2DBC_USERNAME` y `R2DBC_PASSWORD` como variables de entorno. La contraseña que estaba escrita en el `application.yml` original fue retirada de esta preparación; si esa clave era válida, revócala y genera otra en Neon/PostgreSQL. No subas JWT, tokens de SonarQube, secretos de Slack ni contraseñas.
