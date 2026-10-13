# GI08 / Sprint 08 — Pipeline CI/CD del `preparation-service`

Este directorio contiene una implementación reproducible de la actividad PSW:

- `Jenkinsfile`: compila, ejecuta pruebas unitarias, analiza con SonarQube, ejecuta Selenium, ejecuta JMeter y notifica a Slack.
- `src/test/.../PreparationServiceTest.java`: pruebas unitarias del caso de uso.
- `src/test/.../PreparationFunctionalSeleniumIT.java`: dos pruebas funcionales de navegador.
- `performance/preparation-load.jmx`: escenarios de carga Verde, Amarillo y Rojo.
- `application-pipeline.yml` y `schema-pipeline.sql`: perfil aislado para CI con H2; no usa la base real del proyecto.
- `sonar-project.properties`: configuración del análisis y cobertura.
