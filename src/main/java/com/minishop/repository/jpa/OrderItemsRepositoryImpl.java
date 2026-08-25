package com.minishop.repository.jpa;

import com.minishop.domain.order.OrderItem;
import com.minishop.repository.OrderItemsRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class OrderItemsRepositoryImpl implements OrderItemsRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void update(OrderItem orderItem) {
        em.merge(orderItem);
    }
}