package com.back.domain.order.dto;

import jakarta.validation.constraints.NotNull;

public record OrderItemCreateReqBody(
        @NotNull Long productId,
        @NotNull int count
) {
    public OrderItemParam toParam() {
        return new OrderItemParam(productId, count);
    }
}