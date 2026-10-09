package com.xzie.user_service.dto;

import java.math.BigDecimal;

public record ChatProduct
        (Long id, String name, BigDecimal price, Integer stock, String category, String description) {}