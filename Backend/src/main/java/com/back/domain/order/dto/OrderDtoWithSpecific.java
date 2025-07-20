package com.back.domain.order.dto;

import com.back.domain.order.entity.Order;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;


@Schema(description = "주문 상세 정보 DTO")
public record OrderDtoWithSpecific(
        @Schema(description = "주문 ID")
        Long id,
        @Schema(description = "주문자 이메일")
        String customerEmail,
        @Schema(description = "주문자 이름")
        @NonNull String customerName,
        @Schema(description = "주문 배송 주소")
        String customerAddress,
        @Schema(description = "주문 상태 코드", example = "ORDERED", allowableValues = {"ORDERED", "PAID", "SHIPPING", "COMPLETED", "CANCELED"})
        String state,
        @Schema(description = "주문 생성일", example = "2025-07-18 14:00:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdDate,
        @Schema(description = "주문에 포함된 상품 목록")
        List<OrderItemDto> orderItems
) {
    // 주문 상세 조회 (아이템 포함)
    public OrderDtoWithSpecific(Order order) {
        this(
                order.getId(),
                order.getCustomer().getEmail(),
                order.getCustomer().getName(),
                order.getCustomerAddress(),
                order.getStatus().name(),
                order.getCreatedDate(),
                order.getOrderItems().stream().map(OrderItemDto::new).toList()
        );
    }
}
