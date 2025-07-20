package com.back.domain.order.dto;

import com.back.domain.order.entity.OrderItem;

/**
 * 사용자 주문 내역 반환에 사용하는 주문 아이템 DTO 클래스
 */
public record UserOrderItemResponseDto(
        String productName,
        Long productId,
        int count,
        int price
) {
    public UserOrderItemResponseDto(OrderItem orderItem) {
        this(
                orderItem.getProduct().getProductName(),
                orderItem.getProduct().getId(),
                orderItem.getCount(),
                orderItem.getPrice()
        );
    }
}
