package com.jashan.orderservice.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    OrderControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void returnsCreatedOrder() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest(1L, 10L, 2);
        when(orderService.createOrder(request)).thenReturn(new OrderResponse(42L, 1L, 10L, 2));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId": 1, "productId": 10, "quantity": 2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.productId").value(10))
                .andExpect(jsonPath("$.quantity").value(2));

        verify(orderService).createOrder(request);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "{\"customerId\": 1, \"productId\": 10, \"quantity\": 0}",
            "{\"customerId\": 1, \"productId\": 10, \"quantity\": -1}",
            "{\"customerId\": 1, \"productId\": 10}",
            "{\"customerId\": 1, \"quantity\": 2}",
            "{\"productId\": 10, \"quantity\": 2}",
            "{\"customerId\": -1, \"productId\": 10, \"quantity\": 2}",
            "{\"customerId\": 1, \"productId\": 0, \"quantity\": 2}",
            "{\"customerId\": 1, \"productId\": 10, \"quantity\": null}",
            "{\"customerId\": 1, \"productId\": 10, \"quantity\": \"invalid\"}",
            "{"
    })
    void rejectsInvalidRequestWithoutCallingService(String json) throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(orderService);
    }
}
