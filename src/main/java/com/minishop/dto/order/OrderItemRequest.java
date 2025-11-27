// dto/order/OrderItemRequest.java
package com.minishop.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemRequest {
    private Long itemId;
    private int quantity;


}
