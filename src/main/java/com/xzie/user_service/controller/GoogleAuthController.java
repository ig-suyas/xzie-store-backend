package com.xzie.user_service.controller;

import com.xzie.user_service.service.GoogleAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GoogleAuthController {

    @Autowired
    private GoogleAuthService googleAuthService;

    @PostMapping("/auth/google")
    public Map<String, String> google(@RequestBody Map<String, String> body) throws Exception {
        String token = googleAuthService.loginWithGoogle(body.get("idToken"));
        return Map.of("token", token);
    }
}