package com.xzie.user_service.dto;

import java.math.BigDecimal;

public record OrderItemDto(String productName, int quantity, BigDecimal price) {}
