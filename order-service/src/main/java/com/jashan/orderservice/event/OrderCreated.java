package com.jashan.orderservice.event;

public record OrderCreated(Long orderId, Long customerId, Long productId, int quantity) {

    public static final String TOPIC = "order-created";
}


//this is another file