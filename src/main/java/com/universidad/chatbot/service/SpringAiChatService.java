package com.universidad.chatbot.service;

import com.universidad.chatbot.config.ChatDtos.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/** El mismo ChatClient funciona con Groq, OpenAI u Ollama. */
@Service
public class SpringAiChatService {

    private final ChatClient chatClient;

    public SpringAiChatService(ChatClient.Builder builder) {
        this.chatClient = builder.defaultSystem("""
                Eres un asistente experto en {dominio}.
                Responde siempre en español, de forma clara y concisa.
                Si no conoces la respuesta, dilo honestamente.
                No inventes información.
                """).build();
    }

    public ChatResponse chat(String pregunta, String dominio) {
        var response = chatClient.prompt()
                .system(s -> s.param("dominio", dominio))
                .user(pregunta)
                .call()
                .chatResponse();

        if (response == null || response.getResult() == null
                || response.getResult().getOutput().getText() == null
                || response.getResult().getOutput().getText().isBlank()) {
            throw new IllegalStateException("El proveedor devolvió una respuesta vacía.");
        }

        // El modelo proviene de la respuesta real: no queda fijado a Groq.
        return new ChatResponse(response.getResult().getOutput().getText(),
                response.getMetadata().getModel(), dominio);
    }
}
