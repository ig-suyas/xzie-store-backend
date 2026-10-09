package com.xzie.user_service.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import com.xzie.user_service.dao.UserRepo;
import com.xzie.user_service.model.User;
import com.xzie.user_service.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String clientId;

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private PasswordEncoder encoder;

    public String loginWithGoogle(String idToken) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(List.of(clientId)).build();

        GoogleIdToken token = verifier.verify(idToken);
        if (token == null)
            throw new RuntimeException("Invalid Google token");

        GoogleIdToken.Payload p = token.getPayload();
        String email = p.getEmail();
        String name = (String) p.get("name");

        User user = userRepo.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setUsername(name);
            u.setRoles("USER");
            u.setPassword(encoder.encode(UUID.randomUUID().toString()));
            return userRepo.save(u);
        });

        return jwtService.generateToken(user.getUsername());
    }
}