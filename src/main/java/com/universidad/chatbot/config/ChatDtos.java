package com.universidad.chatbot.config;

// ─────────────────────────────────────────────────────────────
//  DTOs (Data Transfer Objects) para el API REST del chatbot
//
//  Se definen como Java Records (Java 16+) para ser inmutables
//  y no requerir Lombok en estos casos puntuales.
// ─────────────────────────────────────────────────────────────

/**
 * Petición que envía el cliente al endpoint POST /api/v2/chat
 *
 * Ejemplo de JSON de entrada:
 * {
 *   "pregunta": "¿Qué es Spring Boot?",
 *   "dominio": "tecnología"
 * }
 */
public class ChatDtos {

    /**
     * Cuerpo de la solicitud HTTP entrante.
     *
     * @param pregunta  Texto que el usuario quiere hacerle al LLM.
     * @param dominio   Dominio temático del asistente (opcional, default: "tecnología").
     */
    public record ChatRequest(
            String pregunta,
            String dominio
    ) {
        // Constructor compacto: aplica defaults si los campos llegan nulos
        public ChatRequest {
            if (dominio == null || dominio.isBlank()) {
                dominio = "tecnología";
            }
            if (pregunta == null || pregunta.isBlank()) {
                throw new IllegalArgumentException("El campo 'pregunta' no puede estar vacío.");
            }
        }
    }

    /**
     * Cuerpo de la respuesta HTTP que devuelve el endpoint.
     *
     * Ejemplo de JSON de salida:
     * {
     *   "respuesta": "Spring Boot es un framework...",
     *   "modelo": "openai/gpt-oss-120b",
     *   "dominio": "tecnología"
     * }
     *
     * @param respuesta Texto generado por el LLM.
     * @param modelo    Nombre del modelo utilizado.
     * @param dominio   Dominio que se usó como contexto.
     */
    public record ChatResponse(
            String respuesta,
            String modelo,
            String dominio
    ) {}
}
