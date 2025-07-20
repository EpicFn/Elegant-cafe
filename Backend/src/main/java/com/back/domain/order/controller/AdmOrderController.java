package com.back.domain.order.controller;

import com.back.domain.order.dto.OrderDtoWithName;
import com.back.domain.order.dto.OrderDtoWithSpecific;
import com.back.domain.order.entity.Order;
import com.back.domain.order.service.OrderService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/adm/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "AdmOrderController", description = "관리자용 주문 API 컨트롤러")
@SecurityRequirement(name = "bearerAuth")
public class AdmOrderController {
    private final OrderService orderService;

    @GetMapping("")
    @Operation(summary = "주문 목록 조회")
    public RsData<List<OrderDtoWithName>> getOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderDtoWithName> dtos = orders.stream()
                .map(OrderDtoWithName::new)
                .toList();
        return new RsData<>(
                200,
                "주문 조회에 성공했습니다.",
                dtos
        );
    }

    // 주문 상세 조회
    @GetMapping("/{orderId}/detail")
    @Operation(summary = "주문 상세 조회")
    public RsData<OrderDtoWithSpecific> getOrderDetail(@PathVariable Long orderId) {
        Order order = orderService.getOrderEntity(orderId);
        return new RsData<>(
                200,
                "주문 상세 조회에 성공했습니다.",
                new OrderDtoWithSpecific(order));
    }

    record OrderStatusReqBody(
            @NotNull String status
    ) {}

    record OrderStatusResBody(
            Long id,
            String status
    ) {}

    // 주문 처리 상태 변경 (주문완료, 배송중, 배송완료, 주문취소)
    @PutMapping("/{orderId}/status")
    @Operation(summary = "주문 상태 변경", description = "주문 상태를 변경합니다. 예: ORDERED, SHIPPING, COMPLETED, CANCELED")
    public RsData<OrderStatusResBody> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderStatusReqBody reqBody) {
        Order order = orderService.updateOrderStatus(orderId, reqBody.status());
        return new RsData<>(
                200,
                "%s번 주문의 상태가 %s로 변경되었습니다.".formatted(orderId, order.getStatus().getDescription()),
                new OrderStatusResBody(
                        order.getId(),
                        order.getStatus().getDescription()
                )
        );
    }

}
