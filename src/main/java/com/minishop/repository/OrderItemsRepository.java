package com.minishop.repository;

import com.minishop.domain.order.OrderItem;

import java.util.List;

public interface OrderItemsRepository {
    void update(OrderItem old);
}
