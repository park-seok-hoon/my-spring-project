package com.minishop.repository;

import com.minishop.domain.OrderItems;
import com.minishop.domain.Orders;

import java.util.List;

public interface OrderRepository {
    void save(Orders order);
    Orders findById(Long id);
    List<Orders> findAll();
    void update(Orders order);
    void updateStatus(Long orderId, String newStatus);
    void updateTotalPrice(Long orderId, int newTotalPrice);
    void updateOrderItems(Long id, List<OrderItems> orderItems);
    void deleteAll();
}
