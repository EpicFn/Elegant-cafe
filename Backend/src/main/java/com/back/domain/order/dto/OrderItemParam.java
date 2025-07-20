package com.back.domain.order.dto;


public record OrderItemParam(
        Long productId,
        int count
) {}
