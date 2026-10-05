# Guía base Spring AI y Groq

Este proyecto expone servicios REST con Spring Boot 3.5 y Java 21. Spring AI utiliza el cliente compatible con OpenAI para enviar solicitudes de chat a Groq. La URL base configurada es https://api.groq.com/openai y el identificador del modelo se define mediante variables de entorno. La clave se proporciona como GROQ_API_KEY y no debe guardarse en el código fuente.

La ruta POST /api/v2/chat recibe un JSON con pregunta y dominio. La ruta GET /api/v2/chat/salud comprueba que la aplicación está disponible, pero no llama al proveedor. La ruta POST /api/v2/rag recibe una pregunta, busca fragmentos pertinentes en los documentos indexados y añade esos fragmentos como contexto antes de pedir la respuesta al modelo. La respuesta RAG incluye el nombre de los documentos fuente.

Los documentos de conocimiento aceptados por la ruta POST /api/v2/rag/documentos son archivos de texto UTF-8 con extensión .txt o .md. El contenido se divide en fragmentos de hasta 850 caracteres, con un solapamiento de 120 caracteres. La recuperación inicial compara los términos de la pregunta con los términos de cada fragmento y entrega hasta tres resultados con relevancia. La ruta GET /api/v2/rag/documentos muestra los nombres y la cantidad de fragmentos.

El flujo RAG consta de recuperación, aumento y generación: se selecciona contexto del índice, se incorpora a la instrucción enviada a Groq y el modelo redacta una respuesta fundamentada. Si no hay fragmentos pertinentes, el servicio informa que los documentos no permiten responder y no consulta el modelo.
