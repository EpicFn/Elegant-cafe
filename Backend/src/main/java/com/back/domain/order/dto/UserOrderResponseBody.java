package com.back.domain.order.dto;

import com.back.domain.order.entity.Order;

/**
 * 사용자 주문 내역 반환에 사용하는 주문 DTO 클래스
 */
public record UserOrderResponseBody(
        Long orderId,
        String orderDate,
        String status,
        String customerAddress,
        UserOrderItemResponseDto[] orderItems
) {
    public UserOrderResponseBody(Order order){
        this(
                order.getId(),
                order.getCreatedDate().toString(),
                order.getStatus().name(),
                order.getCustomerAddress(),
                order.getOrderItems().stream()
                        .map(UserOrderItemResponseDto::new)
                        .toArray(UserOrderItemResponseDto[]::new)
        );
    }
}
