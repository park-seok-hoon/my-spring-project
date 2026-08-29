package com.minishop.dto.order;

import com.minishop.domain.order.OrderItem;
import com.minishop.dto.item.ItemResponse;

public record OrderItemResponse(
        Long id,
        int quantity,
        ItemResponse item) {
    public static OrderItemResponse from(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getId(),
                orderItem.getQuantity(),
                ItemResponse.from(orderItem.getItem())
        );
    }
}
