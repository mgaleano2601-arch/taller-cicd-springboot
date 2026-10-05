package com.universidad.chatbot.controller;

import com.universidad.chatbot.config.ChatDtos.ChatRequest;
import com.universidad.chatbot.config.RagDtos.DocumentInfo;
import com.universidad.chatbot.config.RagDtos.RagResponse;
import com.universidad.chatbot.service.DocumentKnowledgeService;
import com.universidad.chatbot.service.RagChatService;
import java.util.List;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v2/rag")
public class RagController {
    private final DocumentKnowledgeService documents;
    private final RagChatService rag;
    public RagController(DocumentKnowledgeService documents, RagChatService rag) {
        this.documents = documents; this.rag = rag;
    }
    @PostMapping
    public RagResponse ask(@RequestBody ChatRequest request) { return rag.answer(request.pregunta()); }
    @PostMapping("/documentos")
    public DocumentInfo upload(@RequestPart("archivo") MultipartFile file) { return documents.add(file); }
    @GetMapping("/documentos")
    public List<DocumentInfo> list() { return documents.list(); }
    @GetMapping("/salud")
    public String health() { return "RAG activo. Documentos indexados: " + documents.list().size(); }
}
