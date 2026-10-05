package com.universidad.chatbot.controller;

import com.universidad.chatbot.config.ChatDtos.ChatRequest;
import com.universidad.chatbot.config.ChatDtos.ChatResponse;
import com.universidad.chatbot.service.SpringAiChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/chat")
public class SpringAiChatController {

    private final SpringAiChatService chatService;

    public SpringAiChatController(SpringAiChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return chatService.chat(request.pregunta(), request.dominio());
    }

    @PostMapping("/rapido")
    public ChatResponse chatRapido(
            @RequestParam String pregunta,
            @RequestParam(defaultValue = "tecnología") String dominio) {
        // Aplica la misma validación y valores por defecto que el endpoint JSON.
        return chat(new ChatRequest(pregunta, dominio));
    }

    @GetMapping("/salud")
    public String salud() {
        return "Aplicación activa. Este endpoint no comprueba la conexión con el proveedor LLM.";
    }
}
