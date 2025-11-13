// dto/order/OrderItemRequest.java
package com.minishop.dto.order;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long itemId;
    private int quantity;
}
