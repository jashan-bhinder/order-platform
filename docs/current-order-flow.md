# Current implemented order flow

Only the order service currently has business logic. Inventory, payment, and
notification are application skeletons; the README's full architecture diagram
shows the intended system rather than the implemented flow.

```mermaid
sequenceDiagram
    participant Client
    participant Order as order-service
    participant DB as orders_db
    participant Kafka
    Client->>Order: POST /orders
    Note over Order: Validate positive customerId, productId, quantity
    Order->>DB: Save order
    DB-->>Order: Saved order with generated ID (committed)
    Order->>Kafka: OrderCreated JSON on order-created
    Kafka-->>Order: Publish acknowledgment
    Order-->>Client: 201 Created with order
```

If saving fails, no event is published. If publishing fails after saving, the
request fails but the saved order remains. Saving and publishing are not atomic.
No application consumer currently processes the event.
