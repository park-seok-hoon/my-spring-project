package com.minishop.repository.jpa;

import com.minishop.domain.order.Order;
import com.minishop.domain.order.OrderItem;
import com.minishop.domain.order.OrderStatus;
import com.minishop.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepositoryImpl implements OrderRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void save(Order order) {
        em.persist(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        Order order = em.find(Order.class, id);
        return Optional.ofNullable(order);
    }

    public List<Order> findAll() {
        return em.createQuery(
                "select distinct o from Order o " +
                        "join fetch o.user " +
                        "join fetch o.orderItems oi " +
                        "join fetch oi.item",
                Order.class
        ).getResultList();
    }

    @Override
    public void update(Order order) {
        em.merge(order);
    }

    @Override
    public void updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = em.find(Order.class, orderId);

        if (order != null) {
            order.setOrderStatus(newStatus);
        }
    }

    @Override
    public void updateOrderItems(Long id, List<OrderItem> orderItems) {
        Order order = em.find(Order.class, id);

        if (order != null) {
            order.setOrderItems(orderItems);
        }
    }

    @Override
    public void deleteAll() {
        em.createQuery("delete from Order").executeUpdate();
    }
}