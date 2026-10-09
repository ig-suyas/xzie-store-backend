package com.xzie.user_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class StoreKnowledge {

    private final String text;

    public StoreKnowledge(@Value("classpath:store-knowledge.txt") Resource file) throws IOException {
        this.text = new String(file.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public String text() {
        return text;
    }
}