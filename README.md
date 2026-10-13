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

Selecciona una credencial de tipo `Secret text` que contenga un token de SonarCloud con permiso para ejecutar análisis en el proyecto. El pipeline usa esa credencial mediante `withSonarQubeEnv('SonarQube')`.

El error `Failed to connect to localhost:9000` significa que Jenkins todavía tiene configurada una URL local. No se soluciona instalando otro contenedor: hay que cambiar la URL a `https://sonarcloud.io` y volver a ejecutar el pipeline.
