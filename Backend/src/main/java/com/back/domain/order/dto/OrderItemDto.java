package com.back.domain.order.dto;

import com.back.domain.order.entity.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.lang.NonNull;

@Schema(description = "상세 주문 정보 DTO")
public record OrderItemDto(
        @Schema(description = "주문 아이템 ID")
        @NonNull Long id,
        @Schema(description = "주문 ID")
        @NonNull Long orderId,
        @Schema(description = "상품 ID")
        @NonNull Long productId,
        @Schema(description = "상품명")
        @NonNull String productName,
        @Schema(description = "수량")
        @NonNull int count,
        @Schema(description = "낱개 가격")
        @NonNull int price
) {
    public OrderItemDto(OrderItem orderItem) {
        this(
                orderItem.getId(),
                orderItem.getOrder().getId(),
                orderItem.getProduct().getId(),
                orderItem.getProduct().getProductName(),
                orderItem.getCount(),
                orderItem.getPrice()
        );
    }
}
