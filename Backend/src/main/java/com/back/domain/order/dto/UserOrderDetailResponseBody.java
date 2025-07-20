package com.back.domain.order.dto;

import com.back.domain.order.entity.Order;

/**
 * 사용자의 특정 주문 상세 내역 반환에 사용하는 주문 DTO 클래스
 */
public record UserOrderDetailResponseBody(
        Long orderId,
        String orderDate,
        String status,
        String customerAddress,
        UserOrderItemDetailResponseDto[] orderItems
) {
    public UserOrderDetailResponseBody(Order order){
        this(
                order.getId(),
                order.getCreatedDate().toString(),
                order.getStatus().name(),
                order.getCustomerAddress(),
                order.getOrderItems().stream()
                        .map(UserOrderItemDetailResponseDto::new)
                        .toArray(UserOrderItemDetailResponseDto[]::new)
        );
    }
}
