package com.jashan.orderservice.order;

import com.jashan.orderservice.event.OrderCreated;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderCreated> kafkaTemplate;

    public OrderService(OrderRepository orderRepository, KafkaTemplate<String, OrderCreated> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public OrderResponse createOrder(CreateOrderRequest request) {
        // The repository's own transaction commits before publishing to Kafka.
        Order savedOrder = orderRepository.save(
                new Order(request.customerId(), request.productId(), request.quantity()));

        OrderCreated event = new OrderCreated(savedOrder.getId(), savedOrder.getCustomerId(),
                savedOrder.getProductId(), savedOrder.getQuantity());

        // Wait for acknowledgement so a failed send cannot produce a successful HTTP response.
        kafkaTemplate.send(OrderCreated.TOPIC, savedOrder.getId().toString(), event).join();

        return OrderResponse.from(savedOrder);
    }
}
