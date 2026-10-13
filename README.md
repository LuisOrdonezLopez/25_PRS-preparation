# GI08 / Sprint 08

## SonarCloud desde Jenkins

Este proyecto usa SonarCloud (`https://sonarcloud.io`), no un servidor SonarQube local. El análisis utiliza:

```text
sonar.organization=luisordonezlopez
sonar.projectKey=gi08-preparationservice
```

En Jenkins, entra a `Manage Jenkins > System > SonarQube servers` y conserva el nombre de instalación `SonarQube` para que coincida con el `Jenkinsfile`, pero configura:

```text
Server URL: https://sonarcloud.io
```

En `Manage Jenkins > Credentials > System > Global credentials`, crea una credencial de tipo `Secret text` con estos valores:

```text
ID: sonarcloud-token
Secret: el token de SonarCloud
```

El `Jenkinsfile` inyecta esa credencial como `SONAR_TOKEN` durante el análisis. No pegues el token en el `Jenkinsfile`, `pom.xml`, `sonar-project.properties` ni en GitHub.

Para generar el token en SonarCloud: menú de tu cuenta > `My Account` > `Security` > genera un nuevo token y cópialo inmediatamente. En el plan gratuito utiliza un **Personal Access Token (PAT)**; el usuario que lo genera debe tener permiso para ejecutar análisis en `gi08-preparationservice`.

El token que está guardado como secreto de GitHub Actions puede funcionar porque el valor es el mismo, pero Jenkins no puede leer automáticamente los secretos de GitHub. Debes copiar su valor a la credencial de Jenkins o generar uno nuevo.

El error `Failed to connect to localhost:9000` significa que Jenkins todavía tiene configurada una URL local. No se soluciona instalando otro contenedor: hay que cambiar la URL a `https://sonarcloud.io` y volver a ejecutar el pipeline.

No necesitas webhook para que el análisis se ejecute. Deja `ENFORCE_SONAR_GATE=false`; así Jenkins continúa con Selenium y JMeter después de publicar el análisis. El webhook solo es necesario si quieres que `waitForQualityGate` bloquee el pipeline.

Para Selenium remoto, en `Build with Parameters` usa:

```text
SELENIUM_REMOTE_URL=http://selenium-chrome:4444/wd/hub
SELENIUM_BASE_URL=http://jenkins:8097
```

En esta red Docker, `jenkins` es el nombre del contenedor de Jenkins. No uses `127.0.0.1` para el navegador remoto: allí apuntaría al contenedor `selenium-chrome`, no al servicio Spring Boot.
