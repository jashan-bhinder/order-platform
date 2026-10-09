package com.jashan.orderservice.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest(
        @NotNull @Positive Long customerId,
        @NotNull @Positive Long productId,
        @NotNull @Positive Integer quantity
) {
}
