package com.back.domain.order.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.order.dto.OrderDto;
import com.back.domain.order.dto.OrderItemCreateReqBody;
import com.back.domain.order.dto.OrderItemParam;
import com.back.domain.order.entity.Order;
import com.back.domain.order.service.OrderService;
import com.back.global.rq.Rq;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "OrderController", description = "주문 API 컨트롤러")
public class OrderController {

    private final OrderService orderService;
    private final Rq rq;

    public record OrderCreateReqBody(
            @NotBlank String customerAddress,
            @NotEmpty List<OrderItemCreateReqBody> orderItems
    ) {
    }


    @PostMapping
    @Operation(summary = "주문 생성")
    public RsData<OrderDto> createOrder(@Valid @RequestBody OrderCreateReqBody reqBody) {
        Member actor = rq.getActor();
        List<OrderItemParam> orderItemParams = reqBody.orderItems()
                .stream()
                .map(OrderItemCreateReqBody::toParam)
                .toList();
        Order order = orderService.createOrder(
                actor,
                reqBody.customerAddress(),
                orderItemParams
        );
        return new RsData<>(
                201,
                "%s번 주문이 생성되었습니다.".formatted(order.getId()),
                new OrderDto(order)
        );
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "주문 취소")
    public RsData<OrderDto> cancelOrder(@PathVariable Long orderId) {
        Member actor = rq.getActor();
        Order order = orderService.cancelOrder(orderId, actor);
        return new RsData<>(
                200,
                "%d번 주문이 취소되었습니다.".formatted(orderId),
                new OrderDto(order)
        );
    }

    public record OrderUpdateAddressReqBody(
            @NotBlank String newAddress
    ) {
    }

    @PutMapping("/{orderId}/address")
    @Operation(summary = "주문 주소 변경")
    public RsData<String> updateOrderAddress(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderUpdateAddressReqBody reqBody
    ) {
        Member actor = rq.getActor();
        Order updatedOrder = orderService.updateOrderAddress(orderId, reqBody.newAddress(), actor);
        return new RsData<>(
                200,
                "%s번 주문 주소가 변경되었습니다.".formatted(orderId),
                updatedOrder.getCustomerAddress()
        );
    }
}
