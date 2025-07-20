package com.back.domain.order.entity;

public enum OrderStatus {
    ORDERED("주문완료"),
    SHIPPING("배송중"),
    COMPLETED("배송완료"),
    CANCELED("주문취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
