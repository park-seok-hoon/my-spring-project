package com.minishop.repository.mybatis;

import com.minishop.domain.OrderItems;
import com.minishop.repository.OrderItemsRepository;
import com.minishop.repository.mybatis.mapper.OrderItemsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class OrderItemRepositoryMyBatis implements OrderItemsRepository {

    private final OrderItemsMapper mapper;

    @Override
    public void update(OrderItems oldItems) {
        mapper.update(oldItems);
    }
}
