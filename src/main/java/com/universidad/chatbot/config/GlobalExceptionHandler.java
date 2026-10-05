package com.universidad.chatbot.config;

import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        return error(HttpStatus.BAD_REQUEST,
                "Envía un JSON válido con 'pregunta' no vacía y 'dominio' opcional.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleBadRequest(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, "El campo 'pregunta' no puede estar vacío.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        String detail = ex.getMessage() == null ? "" : ex.getMessage();
        String message = "No se pudo obtener respuesta del proveedor. Revisa la conexión y la configuración.";
        if (detail.contains("401") || detail.contains("Unauthorized")) {
            message = "API Key inválida. Revisa GROQ_API_KEY en las variables de entorno.";
        } else if (detail.contains("429")) {
            message = "Límite de consultas alcanzado. Espera y revisa los límites de tu cuenta Groq.";
        } else if (detail.contains("model_not_found") || detail.contains("model_decommissioned")) {
            message = "Modelo no disponible. Configura GROQ_MODEL con un modelo vigente de tu cuenta.";
        }
        // No devolver el cuerpo original del proveedor ni credenciales al cliente.
        return error(HttpStatus.SERVICE_UNAVAILABLE, message);
    }

    private ResponseEntity<Object> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "error", true, "mensaje", message, "timestamp", Instant.now().toString()));
    }
}
