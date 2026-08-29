package com.minishop.dto.order;


import com.minishop.domain.order.Order;
import com.minishop.domain.order.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        LocalDateTime orderDate,
        int totalPrice,
        OrderStatus status,
        List<OrderItemResponse> orderItems
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getOrderDate(),
                order.getTotalPrice(),
                order.getOrderStatus(),
                order.getOrderItems().stream()
                        .map(OrderItemResponse::from)
                        .toList()
        );
    }
}