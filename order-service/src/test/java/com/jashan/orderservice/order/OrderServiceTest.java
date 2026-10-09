package com.jashan.orderservice.order;

import com.jashan.orderservice.event.OrderCreated;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, OrderCreated> kafkaTemplate;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, kafkaTemplate);
    }

    @Test
    void savesOrderBeforePublishingEventWithGeneratedId() {
        stubSavedOrder();
        OrderCreated expectedEvent = new OrderCreated(42L, 1L, 10L, 2);
        when(kafkaTemplate.send(OrderCreated.TOPIC, "42", expectedEvent))
                .thenReturn(CompletableFuture.completedFuture(null));

        OrderResponse response = orderService.createOrder(new CreateOrderRequest(1L, 10L, 2));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        InOrder calls = inOrder(orderRepository, kafkaTemplate);
        calls.verify(orderRepository).save(orderCaptor.capture());
        calls.verify(kafkaTemplate).send(OrderCreated.TOPIC, "42", expectedEvent);
        assertEquals(1L, orderCaptor.getValue().getCustomerId());
        assertEquals(10L, orderCaptor.getValue().getProductId());
        assertEquals(2, orderCaptor.getValue().getQuantity());
        assertEquals(new OrderResponse(42L, 1L, 10L, 2), response);
    }

    @Test
    void doesNotPublishWhenSavingFails() {
        var failure = new DataAccessResourceFailureException("Database unavailable");
        when(orderRepository.save(any(Order.class))).thenThrow(failure);

        var thrown = assertThrows(DataAccessResourceFailureException.class,
                () -> orderService.createOrder(new CreateOrderRequest(1L, 10L, 2)));

        assertSame(failure, thrown);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void propagatesAsynchronousKafkaFailureInsteadOfReturningSuccess() {
        stubSavedOrder();
        var failure = new IllegalStateException("Kafka unavailable");
        when(kafkaTemplate.send(OrderCreated.TOPIC, "42", new OrderCreated(42L, 1L, 10L, 2)))
                .thenReturn(CompletableFuture.failedFuture(failure));

        var thrown = assertThrows(CompletionException.class,
                () -> orderService.createOrder(new CreateOrderRequest(1L, 10L, 2)));

        assertSame(failure, thrown.getCause());
    }

    private void stubSavedOrder() {
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            // Simulate the ID Postgres assigns without adding a public entity ID setter.
            ReflectionTestUtils.setField(order, "id", 42L);
            return order;
        });
    }
}
