package com.universidad.chatbot.service;

import com.universidad.chatbot.config.RagDtos.RagResponse;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class RagChatService {
    private final ChatClient client;
    private final DocumentKnowledgeService knowledge;

    public RagChatService(ChatClient.Builder builder, DocumentKnowledgeService knowledge) {
        this.client = builder.build();
        this.knowledge = knowledge;
    }

    public RagResponse answer(String question) {
        var sources = knowledge.retrieve(question, 3);
        if (sources.isEmpty()) return new RagResponse(
                "No encontré información suficiente en los documentos cargados para responder.", null, false, List.of());
        String context = knowledge.textFor(sources);
        var response = client.prompt()
                .system("Responde en español usando solo el contexto. Si no basta, dilo. No inventes. "
                        + "Incluye referencias entre corchetes a los documentos citados.")
                .user("Contexto recuperado:\n" + context + "\n\nPregunta: " + question)
                .call().chatResponse();
        if (response == null || response.getResult() == null || response.getResult().getOutput().getText() == null)
            throw new IllegalStateException("El proveedor devolvió una respuesta vacía.");
        return new RagResponse(response.getResult().getOutput().getText(), response.getMetadata().getModel(), true, sources);
    }
}
