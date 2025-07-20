package com.back.domain.order.dto;

import com.back.domain.order.entity.OrderItem;

/**
 * 사용자의 특정 주문 상세 내역 반환에 사용하는 주문 아이템 DTO 클래스
 */
public record UserOrderItemDetailResponseDto(
        String productName,
        Long productId,
        String productImageUrl,
        String productCategory,
        int count,
        int price
) {
    public UserOrderItemDetailResponseDto(OrderItem orderItem) {
        this(
                orderItem.getProduct().getProductName(),
                orderItem.getProduct().getId(),
                orderItem.getProduct().getImageUrl(),
                orderItem.getProduct().getCategory(),
                orderItem.getCount(),
                orderItem.getPrice()
        );
    }
}
