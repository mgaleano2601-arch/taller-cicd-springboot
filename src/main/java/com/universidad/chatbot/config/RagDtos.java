package com.universidad.chatbot.config;

import java.util.List;

public class RagDtos {
    public record Source(String documento, int fragmento, double relevancia) {}
    public record RagResponse(String respuesta, String modelo, boolean contextoEncontrado, List<Source> fuentes) {}
    public record DocumentInfo(String nombre, int fragmentos) {}
}
