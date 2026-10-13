# GI08 / Sprint 08 — Pipeline CI/CD del `preparation-service`

> Guía de cierre del trabajo: qué se implementó, cómo se ejecuta y qué problemas tuvimos que resolver en el camino. La idea es que, si vuelves a verlo después de clase, encuentres todo en un solo lugar. 🚀

## 1. ¿Qué hace este proyecto?

El [Jenkinsfile](Jenkinsfile) automatiza este flujo:

```text
Checkout → Maven + pruebas unitarias → SonarCloud → Selenium → JMeter → Slack
```

| Etapa | Resultado que genera |
|---|---|
| Compilación y pruebas unitarias | Compilación Maven, 4 pruebas unitarias y reportes JUnit |
| Análisis de calidad | Análisis de Java en SonarCloud con cobertura JaCoCo |
| Pruebas funcionales | 2 pruebas con Selenium contra Swagger y un endpoint protegido |
| Pruebas de carga | Escenarios Verde, Amarillo y Rojo con JMeter |
| Notificación | Mensaje de resultado en `#gi08-pipeline` |

El proyecto usa un perfil `pipeline` con H2 en memoria. Así Jenkins puede levantar el microservicio sin depender de PostgreSQL, Keycloak ni de otros microservicios externos.

## 2. Archivos importantes

- `Jenkinsfile`: pipeline completo.
- `pom.xml`: perfiles Maven, JaCoCo, Surefire, Failsafe y Selenium.
- `sonar-project.properties`: configuración de SonarCloud.
- `src/main/resources/application-pipeline.yml`: puerto `8097`, escucha en `0.0.0.0` y base H2.
- `src/test/.../PreparationServiceTest.java`: pruebas unitarias.
- `src/test/.../PreparationFunctionalSeleniumIT.java`: pruebas funcionales de navegador.
- `performance/preparation-load.jmx`: escenarios de carga.
- `reports/functional/`: logs de la aplicación y evidencias Selenium.
- `reports/jmeter/`: resultados `.jtl` e informe HTML de JMeter.

## 3. Entorno Docker utilizado 🐳

Jenkins se ejecuta en un contenedor y el navegador se ejecuta en otro contenedor Selenium. Esto fue necesario porque la imagen normal de Jenkins no traía Chrome/Chromium listo para ejecutar pruebas gráficas.

La red usada fue `gi08-ci`:

| Contenedor | Función | Nombre que debe resolverse dentro de la red |
|---|---|---|
| `jenkins` | Ejecuta el pipeline y levanta Spring Boot | `jenkins` |
| `selenium-chrome` | Ejecuta Chrome/Chromium mediante WebDriver remoto | `selenium-chrome` |

La IP `172.19.0.2` que apareció durante las pruebas pertenecía a Jenkins, pero puede cambiar. Es mejor usar los nombres de contenedor.

Si se necesita recrear Selenium:

```powershell
docker network create gi08-ci
docker run -d --name selenium-chrome `
  --network gi08-ci `
  --shm-size=2g `
  -p 4444:4444 `
  selenium/standalone-chrome:4.25.0
```

Si la red o el contenedor ya existen, no se deben crear nuevamente. Comprueba el estado con:

```powershell
docker network inspect gi08-ci
docker run --rm --network gi08-ci curlimages/curl:8.10.1 `
  http://selenium-chrome:4444/status
```

El puerto `8097` solo está disponible mientras Jenkins ejecuta las etapas Selenium o JMeter. Por eso un `curl` a `172.19.0.2:8097` puede fallar antes o después del pipeline: en esos momentos la aplicación no está levantada.

## 4. Configuración previa de Jenkins

El job debe ser de tipo **Pipeline > Pipeline script from SCM** y apuntar a:

```text
Repositorio: https://github.com/LuisOrdonezLopez/25_PRS-preparation.git
Rama: main
Script Path: Jenkinsfile
```

Plugins utilizados:

- Pipeline y Git.
- JUnit.
- SonarQube Scanner for Jenkins.
- Credentials Binding.
- Slack Notification.
- HTML Publisher, opcional para publicar el informe HTML de JMeter.

El contenedor o agente que ejecuta Jenkins debe tener disponibles:

```text
Java 21, Maven, curl y JMeter
```

El proyecto se compila con Java 17, pero Jenkins usa Java 21 para ejecutar el scanner de SonarCloud.

Comprueba las herramientas así:

```powershell
docker exec jenkins java -version
docker exec jenkins mvn -version
docker exec jenkins curl --version
docker exec jenkins jmeter --version  # opcional: el pipeline puede descargarlo
```

Si `jmeter` no existe en el contenedor, el propio `Jenkinsfile` descarga Apache JMeter 5.6.3 en `.tools/` y lo reutiliza en las siguientes ejecuciones. Para ello Jenkins necesita tener `curl`, `tar` y salida a Internet. No es necesario reconstruir el contenedor solo por este motivo.

## 5. SonarCloud 🔐

No se usa un servidor SonarQube local. El análisis se envía a:

```text
https://sonarcloud.io
```

Valores del proyecto:

```text
Organización: luisordonezlopez
Project key: gi08-preparationservice
```

### URL de Jenkins

En `Manage Jenkins > System > SonarQube servers`, puede mantenerse el nombre de instalación `SonarQube`, porque así está escrito en el `Jenkinsfile`, pero la URL debe ser:

```text
https://sonarcloud.io
```

### Token de SonarCloud

En SonarCloud:

```text
Avatar > My Account > Security > Generate Token
```

En Jenkins crea:

```text
Manage Jenkins > Credentials > System > Global credentials > Add Credentials
```

Usa exactamente:

```text
Kind: Secret text
ID: sonarcloud-token
Secret: token generado en SonarCloud
```

El token de GitHub Actions no se comparte automáticamente con Jenkins. Puede reutilizarse el mismo valor, pero hay que copiarlo a la credencial de Jenkins. Nunca lo escribas en el `Jenkinsfile`, `pom.xml`, este README ni en Git.

El pipeline inyecta la credencial como `SONAR_TOKEN`. Si todo está correcto, la consola muestra:

```text
Communicating with SonarQube Cloud
ANALYSIS SUCCESSFUL
BUILD SUCCESS
```

### Quality Gate y webhook

El parámetro `ENFORCE_SONAR_GATE` está en `false` por defecto. Esto permite analizar el código y continuar con Selenium/JMeter sin depender de un webhook de SonarCloud.

Por tanto, este mensaje es normal:

```text
Stage "Quality Gate SonarQube" skipped due to when conditional
```

No es un error. Para esta actividad se puede evidenciar el análisis directamente desde el dashboard de SonarCloud:

<https://sonarcloud.io/dashboard?id=gi08-preparationservice>

## 6. Ejecutar el pipeline ▶️

Usa **Build with Parameters** con estos valores:

```text
JWT_TOKEN=
ENFORCE_SONAR_GATE=false
SELENIUM_REMOTE_URL=http://selenium-chrome:4444/wd/hub
SELENIUM_BASE_URL=http://jenkins:8097
```

### No confundas los tokens

| Nombre | Para qué sirve | ¿Es obligatorio? |
|---|---|---|
| `sonarcloud-token` / `SONAR_TOKEN` | Autenticar el análisis en SonarCloud | Sí |
| `JWT_TOKEN` | Probar endpoints protegidos con JMeter | No |
| Token de Slack | Enviar la notificación al canal | Sí para la notificación |

`JWT_TOKEN` se deja vacío en la configuración normal. El plan JMeter acepta respuestas anónimas `401`, `403` o `404`. Solo se completa si se dispone de un JWT válido para comprobar una respuesta autenticada `200`.

## 7. Escenarios Selenium 🧪

Se ejecutan con `mvn -Pselenium verify` después de levantar Spring Boot.

| Prueba | Funcionalidad | Resultado esperado |
|---|---|---|
| Swagger | Acceso a `/swagger-ui/index.html` | Se encuentra el contenedor de Swagger y carga la interfaz |
| Seguridad | Acceso anónimo a `/api/v1/preparations/request/1` | La aplicación responde `401 Unauthorized` |

El navegador remoto debe usar `http://jenkins:8097`. Desde el contenedor `selenium-chrome`, `127.0.0.1` apunta al propio contenedor Selenium, no a Jenkins.

## 8. Escenarios JMeter 📈

El archivo `performance/preparation-load.jmx` contiene:

| Escenario | Usuarios | Ramp-up | Iteraciones |
|---|---:|---:|---:|
| Verde — carga normal | 5 | 10 s | 10 |
| Amarillo — carga media | 25 | 20 s | 20 |
| Rojo — estrés | 100 | 30 s | 20 |

Jenkins genera:

```text
reports/jmeter/results.jtl
reports/jmeter/html/
```

Para el informe académico se deben tomar de ese reporte los datos reales de tiempo de respuesta, throughput y errores. No se deben inventar métricas.

## 9. Slack 💬

El bloque `post` del `Jenkinsfile` envía el resultado incluso cuando una etapa falla. El canal configurado es:

```text
#gi08-pipeline
```

En Jenkins debe estar configurado el workspace de Slack, el token del plugin Slack y el bot invitado al canal. El mensaje incluye proyecto, número de build, estado y URL.

## 10. Lo que ocurrió y cómo quedó resuelto 🧯

| Problema observado | Causa | Solución aplicada |
|---|---|---|
| Jenkins esperaba un nodo `linux` | No existía ese label | Se cambió a `agent any` |
| Maven trabajaba en `/workspace/null` | `PROJECT_DIR` llegaba nulo | Se estableció `PROJECT_DIR='.'` y detección automática |
| Selenium se ejecutaba antes de levantar la app | Failsafe estaba incluido en `verify` normal | Las pruebas de integración se omiten y solo corren con `-Pselenium` |
| Chrome local fallaba en Jenkins | El contenedor no tenía un navegador estable | Se usó `selenium-chrome` como navegador remoto |
| Selenium no encontraba la aplicación | Se usaba `127.0.0.1` desde otro contenedor | Se usa `http://jenkins:8097` |
| Maven no encontraba `sonar` | El prefijo no estaba resuelto | Se usa el plugin Maven de Sonar con coordenadas y versión fija |
| Jenkins intentaba `localhost:9000` | Se configuró un SonarQube local inexistente | Se apuntó explícitamente a SonarCloud |
| Sonar decía `Not authorized` | El secreto de GitHub no se comparte con Jenkins | Se creó `sonarcloud-token` en Credentials y se inyecta como `SONAR_TOKEN` |
| Selenium esperaba `.swagger-ui` | La clase aparece después del renderizado JavaScript | Se espera el elemento estable `#swagger-ui` |
| JMeter mostraba `JMETER_JWT_TOKEN: parameter not set` | El JWT opcional estaba vacío y `set -u` detenía el script | El pipeline acepta el valor vacío y ejecuta carga anónima |
| JMeter aparecía como `skipped` | Selenium había fallado antes | Al corregir Selenium, JMeter puede ejecutarse |

## 11. Evidencias para la entrega 📸

Conserva estas capturas:

1. Jenkins con las etapas verdes y los resultados JUnit.
2. SonarCloud con Quality Gate, bugs, vulnerabilidades, code smells, cobertura y duplicaciones.
3. Selenium mostrando `Tests run: 2, Failures: 0, Errors: 0`.
4. Informe HTML de JMeter con los tres escenarios.
5. Mensaje de resultado en `#gi08-pipeline`.

La ejecución final esperada es:

```text
Compilar y pruebas unitarias     BUILD SUCCESS
Análisis SonarCloud              ANALYSIS SUCCESSFUL
Quality Gate                     skipped (intencional)
Selenium                         Tests run: 2, Failures: 0, Errors: 0
JMeter                           informe HTML generado
Slack                            notificación en #gi08-pipeline
```

✅ Si aparece `Selected Git installation does not exist`, `No suitable checks publisher found` o los avisos de Mockito, no son el fallo principal mientras las etapas terminen correctamente.
