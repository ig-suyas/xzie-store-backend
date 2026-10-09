package com.xzie.user_service.dto;

import java.math.BigDecimal;

public record CartItemDto(Long id, Long productId, String name,
                          BigDecimal price, boolean hasImage, int quantity) {}
