package com.xzie.user_service.controller;

import com.xzie.user_service.dto.ChatRequest;
import com.xzie.user_service.service.ChatRateLimiter;
import com.xzie.user_service.service.ChatService;
import com.xzie.user_service.service.GeminiClient;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@RestController
public class ChatController {

    @Autowired
    private GeminiClient gemini;
    @Autowired private ChatService chat;
    @Autowired private ChatRateLimiter limiter;
    private final JsonMapper mapper = JsonMapper.builder().build();

    @PostMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void stream(@RequestBody ChatRequest req, Authentication auth, HttpServletResponse response) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        PrintWriter w = response.getWriter();

        String message = req.message() == null ? "" : req.message().trim();
        if (message.isEmpty() || message.length() > 500) {
            send(w, "error", Map.of("message", "Please send a message of up to 500 characters."));
            return;
        }
        if (!limiter.allow(auth.getName())) {
            send(w, "error", Map.of("message", "You're sending messages too fast. Please wait a moment."));
            return;
        }
        try {
            gemini.stream(chat.systemPrompt(), chat.trim(req.history()), message,
                    text -> send(w, "message", Map.of("t", text)));
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage() != null && (e.getMessage().contains("503") || e.getMessage().contains("429"))
                    ? "The assistant is very busy right now. Please try again in a minute."
                    : "The assistant is unavailable right now. Please try again.";
            send(w, "error", Map.of("message", msg));
        }
    }

    private void send(PrintWriter w, String event, Map<String, String> data) {
        w.write("event: " + event + "\ndata: " + mapper.writeValueAsString(data) + "\n\n");
        w.flush();
        if (w.checkError()) throw new RuntimeException("Client disconnected");
    }
}