package com.xzie.user_service.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.xzie.user_service.dto.ChatTurn;

@Service
public class GeminiClient {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GeminiClient.class);

    @Value("${gemini.api-key}")  private String apiKey;
    @Value("${gemini.model}")    private String model;
    @Value("${gemini.fallback-model:}") private String fallbackModel;
    @Value("${gemini.base-url}") private String baseUrl;

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final JsonMapper mapper = JsonMapper.builder().build();


    @JsonIgnoreProperties(ignoreUnknown = true) public record Chunk(List<Cand> candidates) {}
    @JsonIgnoreProperties(ignoreUnknown = true) public record Cand(Content content) {}
    @JsonIgnoreProperties(ignoreUnknown = true) public record Content(List<Part> parts) {}
    @JsonIgnoreProperties(ignoreUnknown = true) public record Part(String text) {}

    public void stream(String system, List<ChatTurn> history, String message, Consumer<String> onText) throws Exception {
        if (apiKey == null || apiKey.isBlank()) throw new IllegalStateException("GEMINI_API_KEY is not set");

        List<Map<String, Object>> contents = new ArrayList<>();
        if (history != null) {
            for (ChatTurn t : history) {
                if (t == null || t.text() == null || t.text().isBlank()) continue;
                contents.add(turn("bot".equals(t.role()) ? "model" : "user", t.text()));
            }
        }
        contents.add(turn("user", message));

        String json = mapper.writeValueAsString(Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", system))),
                "contents", contents));

        List<String> models = new ArrayList<>(List.of(model));
        if (fallbackModel != null && !fallbackModel.isBlank()) models.add(fallbackModel);

        HttpResponse<Stream<String>> res = null;
        int status = 0;
        outer:
        for (String m : models) {
            for (int attempt = 0; attempt < 3; attempt++) {
                res = http.send(request(m, json), BodyHandlers.ofLines());
                status = res.statusCode();
                if (status == 200) break outer;
                try (Stream<String> lines = res.body()) {
                    log.warn("Gemini error {} ({}): {}", status, m, String.join(" ", lines.toList()));
                }
                if (status != 503 && status != 429 && status != 500) break;
                Thread.sleep(1000L * (attempt + 1));
            }
        }
        if (res == null || status != 200) throw new RuntimeException("Gemini " + status);

        try (Stream<String> lines = res.body()) {
            Iterator<String> it = lines.iterator();
            while (it.hasNext()) {
                String line = it.next();
                if (!line.startsWith("data:")) continue;
                String data = line.substring(5).trim();
                if (data.isEmpty()) continue;
                Chunk c = mapper.readValue(data, Chunk.class);
                if (c.candidates() == null || c.candidates().isEmpty()) continue;
                Content content = c.candidates().get(0).content();
                if (content == null || content.parts() == null) continue;
                for (Part p : content.parts()) {
                    if (p.text() != null && !p.text().isEmpty()) onText.accept(p.text());
                }
            }
        }
    }

    private HttpRequest request(String m, String json) {
        return HttpRequest.newBuilder(URI.create(baseUrl + "/models/" + m + ":streamGenerateContent?alt=sse"))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .timeout(Duration.ofSeconds(90))
                .POST(BodyPublishers.ofString(json))
                .build();
    }

    private static Map<String, Object> turn(String role, String text) {
        return Map.of("role", role, "parts", List.of(Map.of("text", text)));
    }
}
