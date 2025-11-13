// repository/mybatis/MyBatisOrderRepository.java
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
public class MyBatisOrderRepository implements OrderRepository {

    private final OrderMapper orderMapper;

    @Override
    public void save(Orders order) {
        orderMapper.insertOrder(order);
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
    public void cancel(Long orderId) {
        orderMapper.updateOrderStatus(orderId, "CANCELLED");
    }

}
