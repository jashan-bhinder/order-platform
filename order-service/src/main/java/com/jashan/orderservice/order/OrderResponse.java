package com.jashan.orderservice.order;

public record OrderResponse(Long id, Long customerId, Long productId, int quantity) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getCustomerId(), order.getProductId(), order.getQuantity());
    }
}
