// repository/mybatis/OrderRepositoryMyBatis.java
package com.minishop.repository.mybatis;

import com.minishop.domain.Orders;
import com.minishop.domain.OrderItems;
import com.minishop.repository.OrderRepository;
import com.minishop.repository.mybatis.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryMyBatis implements OrderRepository {

    private final OrderMapper orderMapper;

    @Override
    public void save(Orders order) {
        orderMapper.insertOrder(order);

        // 1개의 주문에 여러 개의 상품을 주문
        for (OrderItems item : order.getOrderItems()) {
            item.setOrderId(order.getId());
            orderMapper.insertOrderItem(item);
        }
    }

    @Override
    public Orders findById(Long id) {
        return orderMapper.findById(id);
    }

    @Override
    public List<Orders> findAll() {
        return orderMapper.findAll();
    }

    @Override
    public void update(Orders order) {
        orderMapper.updateOrder(order);
    }

    @Override
    public void updateStatus(Long orderId, String newStatus) {
        orderMapper.updateOrderStatus(orderId, newStatus);
    }

    @Override
    public void updateTotalPrice(Long orderId, int newTotalPrice) {
        orderMapper.updateTotalPrice(orderId, newTotalPrice);
    }

    @Override
    public void updateOrderItems(Long id, List<OrderItems> orderItems) {
        orderMapper.updateOrderItems(id, orderItems);
    }

    @Override
    public void deleteAll() {
        orderMapper.deleteAll();
    }


}
