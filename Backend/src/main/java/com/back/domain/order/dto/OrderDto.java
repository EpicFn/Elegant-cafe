package com.back.domain.order.dto;

import com.back.domain.order.entity.Order;
import com.back.domain.order.entity.OrderStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문 정보 DTO")
public record OrderDto(
        @Schema(description = "주문 ID")
        @NonNull Long id,
        @Schema(description = "주문자 이메일")
        @NonNull String customerEmail,
        @Schema(description = "주문 날짜")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        @NonNull LocalDateTime createdDate,
        @Schema(description = "처리 상태", example = "ORDERED", allowableValues = {"ORDERED", "PAID", "SHIPPING", "COMPLETED", "CANCELED"})
        @NonNull OrderStatus state,
        @Schema(description = "주문 주소")
        @NonNull String customerAddress,
        @Schema(description = "주문 상세 목록")
        @JsonInclude(JsonInclude.Include.NON_NULL) // null인 경우 JSON 응답에서 제외
        @NonNull List<OrderItemDto> orderItems
) {
    public OrderDto(Order order) {
        this(
                order.getId(),
                order.getCustomer().getEmail(),
                order.getCreatedDate(),
                order.getStatus(),
                order.getCustomerAddress(),
                null
        );
    }

}