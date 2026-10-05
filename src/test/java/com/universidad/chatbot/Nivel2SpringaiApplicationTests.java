package com.universidad.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Usa ChatClient real contra un servidor HTTP local, sin llamar ni gastar en Groq. */
@SpringBootTest
@AutoConfigureMockMvc
class Nivel2SpringaiApplicationTests {

    private static final LinkedBlockingQueue<String> requests = new LinkedBlockingQueue<>();
    private static volatile int providerStatus = 200;
    private static volatile String providerBody;
    private static final HttpServer provider = createProvider();

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired com.universidad.chatbot.service.DocumentKnowledgeService knowledge;

    private static HttpServer createProvider() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/openai/v1/chat/completions", exchange -> {
                requests.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                byte[] body = providerBody.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(providerStatus, body.length);
                try (var output = exchange.getResponseBody()) {
                    output.write(body);
                }
            });
            server.start();
            return server;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.ai.openai.api-key", () -> "clave-ficticia-solo-tests");
        registry.add("spring.ai.openai.chat.options.model", () -> "modelo-configurado-en-test");
        registry.add("spring.ai.openai.base-url",
                () -> "http://127.0.0.1:" + provider.getAddress().getPort() + "/openai");
        registry.add("spring.ai.retry.max-attempts", () -> 1);
    }

    @BeforeEach
    void resetProvider() {
        requests.clear();
        providerStatus = 200;
        providerBody = """
                {"id":"chat-test","object":"chat.completion","created":1,
                 "model":"modelo-devuelto-por-proveedor",
                 "choices":[{"index":0,"message":{"role":"assistant","content":"Respuesta simulada."},
                             "finish_reason":"stop"}],
                 "usage":{"prompt_tokens":10,"completion_tokens":5,"total_tokens":15}}
                """;
    }

    @AfterAll
    static void stopProvider() {
        provider.stop(0);
    }

    @Test
    void jsonUsesRealChatClientAndReportsActualModel() throws Exception {
        mvc.perform(post("/api/v2/chat").contentType("application/json")
                        .content("""
                                {"pregunta":"¿Qué es Spring Boot?","dominio":"Java"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.respuesta").value("Respuesta simulada."))
                .andExpect(jsonPath("$.modelo").value("modelo-devuelto-por-proveedor"))
                .andExpect(jsonPath("$.dominio").value("Java"));
        JsonNode sent = mapper.readTree(requests.remove());
        assertThat(sent.get("messages").toString()).contains("Java", "¿Qué es Spring Boot?");
        assertThat(sent.get("messages").toString()).doesNotContain("{dominio}");
        assertThat(sent.get("model").asText()).isEqualTo("modelo-configurado-en-test");
    }

    @Test
    void rapidoReturnsSameContract() throws Exception {
        mvc.perform(post("/api/v2/chat/rapido").param("pregunta", "Explica Maven").param("dominio", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dominio").value("Java"))
                .andExpect(jsonPath("$.modelo").value("modelo-devuelto-por-proveedor"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"pregunta\":\"Hola\"}", "{\"pregunta\":\"Hola\",\"dominio\":\"\"}"})
    void defaultsDomain(String body) throws Exception {
        mvc.perform(post("/api/v2/chat").contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dominio").value("tecnología"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"pregunta\":null}", "{\"pregunta\":\"  \"}", "{no-json"})
    void rejectsInvalidJsonWithoutCallingProvider(String body) throws Exception {
        mvc.perform(post("/api/v2/chat").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(true));
        assertThat(requests).isEmpty();
    }

    @Test
    void rejectsBlankQuery() throws Exception {
        mvc.perform(post("/api/v2/chat/rapido").param("pregunta", " "))
                .andExpect(status().isBadRequest());
        assertThat(requests).isEmpty();
    }

    @Test
    void rejectsMissingQuery() throws Exception {
        mvc.perform(post("/api/v2/chat/rapido")).andExpect(status().isBadRequest());
        assertThat(requests).isEmpty();
    }

    @Test
    void preservesMethodNotAllowed() throws Exception {
        mvc.perform(get("/api/v2/chat")).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void healthDoesNotPretendToCheckProvider() throws Exception {
        mvc.perform(get("/api/v2/chat/salud"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("no comprueba")));
        assertThat(requests).isEmpty();
    }

    @Test
    void estadoEndpointConfirmsApplicationAvailability() throws Exception {
        mvc.perform(get("/api/estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ok"))
                .andExpect(jsonPath("$.mensaje").value("Aplicación disponible"));
        assertThat(requests).isEmpty();
    }

    @Test
    void ragRetrievesDocumentAndAddsItToModelPrompt() throws Exception {
        mvc.perform(post("/api/v2/rag").contentType("application/json")
                        .content("{\"pregunta\":\"¿Qué puerto utiliza el servidor?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contextoEncontrado").value(true))
                .andExpect(jsonPath("$.fuentes[0].documento").value("spring-ai-groq.md"))
                .andExpect(jsonPath("$.modelo").value("modelo-devuelto-por-proveedor"));
        JsonNode sent = mapper.readTree(requests.remove());
        assertThat(sent.get("messages").toString()).contains("850 caracteres", "servidor");
    }

    @Test
    void ragDeclinesWhenNoDocumentMatchesAndSkipsProvider() throws Exception {
        mvc.perform(post("/api/v2/rag").contentType("application/json")
                        .content("{\"pregunta\":\"¿Cuál es la capital de Islandia?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contextoEncontrado").value(false))
                .andExpect(jsonPath("$.fuentes").isEmpty())
                .andExpect(jsonPath("$.modelo").doesNotExist());
        assertThat(requests).isEmpty();
    }

    @Test
    void documentEndpointListsBundledKnowledge() throws Exception {
        mvc.perform(get("/api/v2/rag/documentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("spring-ai-groq.md"))
                .andExpect(jsonPath("$[0].fragmentos").value(3));
    }

    @ParameterizedTest
    @CsvSource({"401,invalid_api_key,API Key", "429,rate_limit_exceeded,Límite",
            "400,model_decommissioned,Modelo"})
    void providerErrorsAreFriendlyAndDoNotLeakDetails(int status, String code, String message)
            throws Exception {
        providerStatus = status;
        providerBody = "{\"error\":{\"message\":\"detalle-privado\",\"code\":\"" + code + "\"}}";
        mvc.perform(post("/api/v2/chat").contentType("application/json")
                        .content("{\"pregunta\":\"Hola\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.mensaje", containsString(message)))
                .andExpect(jsonPath("$.detalle").doesNotExist());
    }

    @Test
    void emptyProviderResponseIsAnError() throws Exception {
        providerBody = providerBody.replace("Respuesta simulada.", "");
        mvc.perform(post("/api/v2/chat").contentType("application/json")
                        .content("{\"pregunta\":\"Hola\"}"))
                .andExpect(status().isServiceUnavailable());
    }
}
