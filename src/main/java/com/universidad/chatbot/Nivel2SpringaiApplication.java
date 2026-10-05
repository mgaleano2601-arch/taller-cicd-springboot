package com.universidad.chatbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación Spring Boot.
 *
 * ANTES DE EJECUTAR:
 *   1. Tener Docker Desktop corriendo
 *   2. Levantar Ollama:
 *        docker run -d -p 11434:11434 --name ollama ollama/ollama
 *   3. Descargar el modelo:
 *        docker exec ollama ollama pull llama3.2:1b
 *   4. Ejecutar esta clase (Run → Nivel2SpringaiApplication)
 */
@SpringBootApplication
public class Nivel2SpringaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(Nivel2SpringaiApplication.class, args);
    }
}
