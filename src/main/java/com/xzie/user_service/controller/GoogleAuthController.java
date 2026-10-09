package com.xzie.user_service.controller;

import com.xzie.user_service.service.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GoogleAuthController {

    @Autowired
    private GoogleAuthService googleAuthService;

    @PostMapping("/auth/google")
    public ResponseEntity<?> google(@RequestBody Map<String, String> body) throws Exception {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "idToken is required"));
        }

        String token = googleAuthService.loginWithGoogle(idToken);
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid Google token"));
        }

        return ResponseEntity.ok(Map.of("token", token));
    }
}