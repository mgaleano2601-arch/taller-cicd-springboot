package com.universidad.chatbot.service;

import com.universidad.chatbot.config.RagDtos.DocumentInfo;
import com.universidad.chatbot.config.RagDtos.Source;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/** Índice RAG local: divide archivos de texto en fragmentos y recupera por coincidencia léxica. */
@Service
public class DocumentKnowledgeService {
    private static final int CHUNK_SIZE = 850;
    private static final int OVERLAP = 120;
    private static final Pattern WORDS = Pattern.compile("[\\p{L}\\p{N}]{3,}");
    private final Map<String, List<Chunk>> documents = new ConcurrentHashMap<>();

    public DocumentKnowledgeService() {
        loadBundledDocuments();
    }

    public synchronized void loadBundledDocuments() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:knowledge/*.md");
            for (Resource resource : resources) {
                try (var in = resource.getInputStream()) {
                    index(resource.getFilename(), new String(in.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudieron cargar los documentos de ejemplo.", ex);
        }
    }

    public synchronized DocumentInfo add(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Selecciona un archivo con contenido.");
        String name = file.getOriginalFilename() == null ? "documento.txt" : file.getOriginalFilename();
        if (!(name.toLowerCase(Locale.ROOT).endsWith(".txt") || name.toLowerCase(Locale.ROOT).endsWith(".md")))
            throw new IllegalArgumentException("Formato no admitido. Usa archivos .txt o .md en UTF-8.");
        try {
            return index(name, new String(file.getBytes(), StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalArgumentException("No se pudo leer el archivo enviado.");
        }
    }

    public List<DocumentInfo> list() {
        return documents.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .map(e -> new DocumentInfo(e.getKey(), e.getValue().size())).toList();
    }

    public List<Source> retrieve(String query, int limit) {
        Set<String> terms = tokens(query);
        if (terms.isEmpty()) return List.of();
        return documents.values().stream().flatMap(Collection::stream)
                .map(chunk -> new Scored(chunk, score(terms, chunk.text)))
                .filter(s -> s.score >= 0.12)
                .sorted(Comparator.comparingDouble(Scored::score).reversed())
                .limit(limit)
                .map(s -> new Source(s.chunk.document, s.chunk.number, Math.round(s.score * 1000.0) / 1000.0))
                .toList();
    }

    public String textFor(List<Source> sources) {
        return sources.stream().map(source -> documents.getOrDefault(source.documento(), List.of()).stream()
                .filter(c -> c.number == source.fragmento()).findFirst().map(c -> "[" + c.document + ", fragmento "
                        + c.number + "]\n" + c.text).orElse("")).filter(s -> !s.isBlank())
                .reduce((a, b) -> a + "\n\n" + b).orElse("");
    }

    private synchronized DocumentInfo index(String name, String text) {
        if (text == null || text.isBlank()) throw new IllegalArgumentException("El documento no contiene texto.");
        List<Chunk> chunks = new ArrayList<>();
        for (int start = 0, number = 1; start < text.length(); start += CHUNK_SIZE - OVERLAP, number++) {
            int end = Math.min(text.length(), start + CHUNK_SIZE);
            String part = text.substring(start, end).trim();
            if (!part.isBlank()) chunks.add(new Chunk(name, number, part));
        }
        documents.put(name, List.copyOf(chunks));
        return new DocumentInfo(name, chunks.size());
    }

    private static Set<String> tokens(String text) {
        String normalized = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        var matcher = WORDS.matcher(normalized);
        Set<String> result = new HashSet<>();
        while (matcher.find()) result.add(matcher.group());
        return result;
    }

    private static double score(Set<String> query, String text) {
        Set<String> content = tokens(text);
        if (content.isEmpty()) return 0;
        long matches = query.stream().filter(content::contains).count();
        return (double) matches / query.size();
    }

    private record Chunk(String document, int number, String text) {}
    private record Scored(Chunk chunk, double score) {}
}
