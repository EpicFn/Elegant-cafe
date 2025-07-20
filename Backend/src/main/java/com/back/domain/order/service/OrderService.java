package com.back.domain.order.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.order.dto.OrderItemParam;
import com.back.domain.order.entity.Order;
import com.back.domain.order.entity.OrderItem;
import com.back.domain.order.entity.OrderStatus;
import com.back.domain.order.repository.OrderRepository;
import com.back.domain.product.entity.Product;
import com.back.domain.product.repository.ProductRepository;
import com.back.global.exception.ServiceException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Transactional
    public Order createOrder(Member actor, String customerAddress, List<OrderItemParam> OrderItemParam) {

        Order order = new Order(actor, customerAddress);

        for (OrderItemParam param : OrderItemParam) {
            Product product = productRepository.findById(param.productId())
                    .orElseThrow(() -> new ServiceException(404, "존재하지 않는 상품입니다."));

            if (!product.isOrderable())
                throw new ServiceException(400, "주문 불가능한 상품입니다.");

            OrderItem orderItem = new OrderItem(order, product, param.count(), product.getPrice());
            order.addOrderItem(orderItem);
        }

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderEntity(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException(404, "해당 주문이 존재하지 않습니다."));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order cancelOrder(Long orderId, Member actor) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException(404, "해당 주문이 존재하지 않습니다."));

        Member customer = order.getCustomer();
        if (customer == null)
            throw new ServiceException(400, "주문에 고객 정보가 없습니다.");

        // 주문 취소 권한 체크 : 주문한 고객이거나, 관리자일 때만 취소 가능
        else if (!customer.getId().equals(actor.getId()) && !actor.isAdmin())
            throw new ServiceException(403, "%d번 주문 취소 권한이 없습니다.".formatted(order.getId()));

        if (order.isCanceled())
            throw new ServiceException(409, "이미 취소된 주문입니다.");


        order.changeStatus(OrderStatus.CANCELED);
        return order;
    }

    @Transactional
    public Order updateOrderAddress(Long orderId, String newAddress, Member actor) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException(404, "해당 주문이 존재하지 않습니다."));

        if (!order.getCustomer().getId().equals(actor.getId()))
            throw new ServiceException(403, "%d번 주문 주소 변경 권한이 없습니다.".formatted(order.getId()));

        if (order.isCanceled())
            throw new ServiceException(409, "이미 취소된 주문입니다.");

        order.changeCustomerAddress(newAddress);
        orderRepository.save(order);
        return order;
    }

    public Optional<Order> findLatest() {
        return orderRepository.findFirstByOrderByIdDesc();
    }

    public Order updateOrderStatus(Long orderId, @NotNull String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException(404, "해당 주문이 존재하지 않습니다."));

        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new ServiceException(400, "유효하지 않은 주문 상태입니다.");
        }

        order.changeStatus(orderStatus);
        return orderRepository.save(order);
    }
}
