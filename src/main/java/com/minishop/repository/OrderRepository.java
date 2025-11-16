package com.minishop.repository;

import com.minishop.domain.Orders;

import java.util.List;

public interface OrderRepository {
    void save(Orders order);
    Orders findById(Long id);
    List<Orders> findAll();
    void update(Orders order);
    void cancel(Long orderId);
    void updateStatus(Long orderId, String newStatus);
}