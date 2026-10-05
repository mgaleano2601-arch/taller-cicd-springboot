# Actividad RAG Spring Boot y Groq

Este proyecto añade búsqueda de contexto a la aplicación Spring AI del nivel 2. La implementación indexa un documento de ejemplo y permite cargar Markdown o texto UTF-8. Recupera hasta tres fragmentos por coincidencia léxica, los envía al modelo Groq y devuelve las fuentes. Si no encuentra contexto pertinente, informa que no puede responder con los documentos y no consulta el modelo.

## Requisitos

- Windows PowerShell y JDK 21 en PATH.
- Una clave de Groq activa, introducida al iniciar. No la incluyas en archivos ni capturas.
- Puerto 8081 disponible.

## Iniciar

Doble clic en `iniciar-rag.cmd`, pega la clave en la ventana y deja el servidor ejecutándose. Si el puerto está ocupado, libera el puerto o ejecuta `iniciar-rag.ps1 -Puerto 8082`.

Salud RAG: http://127.0.0.1:8081/api/v2/rag/salud
Chat base: http://127.0.0.1:8081/api/v2/chat/salud

La ruta de salud solo confirma que la aplicación arrancó; las llamadas de chat/RAG validan conexión, credencial y disponibilidad del modelo.

## Cargar y revisar documentos

En una ventana PowerShell aparte, situada en esta carpeta:

```powershell
$form = @{ archivo = Get-Item .\src\main\resources\knowledge\spring-ai-groq.md }
Invoke-RestMethod -Method Post -Uri http://127.0.0.1:8081/api/v2/rag/documentos -Form $form
Invoke-RestMethod http://127.0.0.1:8081/api/v2/rag/documentos
```

La carga acepta `.txt` y `.md` en UTF-8. El índice está en memoria y se reconstruye al iniciar; al reiniciar, vuelve a cargar el documento incluido y se pierden documentos enviados durante la sesión.

## Consultar RAG

```powershell
$body = @{ pregunta = '¿Qué puerto utiliza la aplicación?' } | ConvertTo-Json
Invoke-RestMethod -Method Post `
  -Uri http://127.0.0.1:8081/api/v2/rag `
  -ContentType 'application/json; charset=utf-8' `
  -Body ([Text.Encoding]::UTF8.GetBytes($body))
```

La respuesta entrega `respuesta`, `modelo`, `contextoEncontrado` y `fuentes`. También se puede usar el proyecto incluido con IntelliJ como proyecto Maven. Para verificar localmente: `mvn verify`. Las pruebas no requieren clave ni acceso externo y usan un proveedor simulado.

## Capturar una consulta RAG real con Groq

Para generar la evidencia en vivo, ejecuta `capturar-evidencia-rag.cmd` dentro de la carpeta `proyecto-rag`. El script solicita la clave Groq de forma oculta, inicia el servidor, carga el Markdown de ejemplo y envía una pregunta que debe recuperar contexto. La clave no se escribe en el archivo de evidencia.

Guarda una captura de la ventana donde se vean `contextoEncontrado: true`, el modelo y `fuentes`. El resultado JSON queda en `evidencias-groq/05-respuesta-rag-real.json`. Si ya hay una instancia RAG en el puerto 8081, el script la reutiliza; si hay otro servicio, busca un puerto cercano libre.

## Archivos clave

- `DocumentKnowledgeService.java`: lectura, fragmentación e índice léxico en memoria.
- `RagChatService.java`: recuperación de hasta tres fragmentos y generación mediante Spring AI.
- `RagController.java`: endpoints REST RAG y carga multipart.
- `src/main/resources/knowledge/spring-ai-groq.md`: conocimiento inicial.
- `Nivel2SpringaiApplicationTests.java`: pruebas del chat, la recuperación RAG, las fuentes y la respuesta sin contexto.
- `evidencias-groq/verificacion-rag.txt`: extracto de la verificación final con 19 pruebas correctas.

Esta entrega admite texto y Markdown, con búsqueda por coincidencia de términos. No incluye extracción de PDF/DOCX ni búsqueda por embeddings.
