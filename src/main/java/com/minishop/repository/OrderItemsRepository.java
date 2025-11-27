package com.minishop.repository;

import com.minishop.domain.OrderItems;

import java.util.List;

public interface OrderItemsRepository {
    void update(OrderItems old);
}
