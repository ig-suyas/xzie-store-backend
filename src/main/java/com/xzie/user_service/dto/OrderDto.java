package com.xzie.user_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(Long id, String status, BigDecimal total,
                       LocalDateTime createdAt, String shippingAddress, String username, List<OrderItemDto> items) {}
