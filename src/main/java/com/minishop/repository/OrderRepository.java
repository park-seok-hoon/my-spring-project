package com.minishop.repository;

import com.minishop.domain.order.Order;
import com.minishop.domain.order.OrderItem;
import com.minishop.domain.order.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    void save(Order order);

    Optional<Order> findById(Long id);

    List<Order> findAll();

    void update(Order order);

    void updateStatus(Long orderId, OrderStatus newStatus);

    void updateOrderItems(Long id, List<OrderItem> orderItems);

    void deleteAll();
}