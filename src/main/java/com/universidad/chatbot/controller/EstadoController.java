package com.universidad.chatbot.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpoint pequeño de ejemplo para validar el flujo feature branch y Pull Request. */
@RestController
public class EstadoController {

    @GetMapping("/api/estado")
    public Map<String, String> estado() {
        return Map.of("estado", "ok", "mensaje", "Aplicación disponible");
    }
}
