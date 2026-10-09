package com.xzie.user_service.dto;

import java.util.List;

public record ChatRequest(String message, List<ChatTurn> history) {}
