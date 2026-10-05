# Taller CI/CD Spring Boot - guía de entrega

El proyecto base es la aplicación Spring Boot + Spring AI + Groq/RAG de este paquete. Esta carpeta es el código preparado para el taller CI/CD: workflow, pruebas JUnit y cobertura JaCoCo.

## Preparación local

1. Comprueba que Java 21 y Maven 3.9 estén instalados.
2. Desde la raíz ejecuta `mvn clean verify`. Debe finalizar en `BUILD SUCCESS`, mostrar 19 pruebas sin fallos y crear `target/site/jacoco/index.html`.
3. Revisa `.gitignore`: excluye compilados, archivos de IntelliJ, variables locales, claves y registros.
4. Revisa que no haya claves Groq en los archivos que se van a subir. La aplicación lee la clave desde `GROQ_API_KEY`.

## Publicación del repositorio

1. Crea o selecciona un repositorio vacío en tu cuenta GitHub.
2. En la raíz del proyecto ejecuta `git init -b main`, agrega los archivos y crea el commit inicial.
3. Configura el remoto con la URL de TU repositorio y ejecuta `git push -u origin main`.
4. Abre la pestaña Actions y conserva la captura del workflow verde. Debe aparecer el job `build`.
5. En la ejecución descarga `reporte-jacoco` y guarda una captura de `index.html` con el porcentaje de cobertura.

## Secretos y rama protegida

1. En Settings > Secrets and variables > Actions agrega `APP_ENV_DEMO`. Usa el valor de demostración indicado por el taller; no uses aquí tu clave de Groq.
2. En Settings > Branches configura `main` para exigir que pase el status check `build` antes de fusionar.
3. Crea `feature/endpoint-saludo`, añade un cambio pequeño, sube la rama y abre un Pull Request hacia `main`.
4. Guarda capturas del check del PR en verde y de la configuración del secreto sin mostrar su valor.
5. Fusiona el PR solo después de que termine el workflow. Copia la URL del PR fusionado al informe.

## Workflow

`.github/workflows/ci.yml` ejecuta `mvn -B clean verify` al recibir push a `main` o `develop` y en cada Pull Request hacia `main`. Publica `target/site/jacoco` como artefacto `reporte-jacoco`. Si `APP_ENV_DEMO` existe, el job confirma que está configurado sin imprimir el valor.

## Estado preparado en este paquete

- Workflow CI, Maven/Java 21 y publicación de JaCoCo: configurados.
- Suite de 19 pruebas JUnit: existente.
- Rama de protección, Actions ejecutado en GitHub, artefacto descargable y PR fusionado: requieren acceso a un repositorio GitHub con permisos de escritura. No se deben representar como completados hasta tener el enlace y las capturas reales.
