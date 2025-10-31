package com.minishop.repository.mybatis;

import com.minishop.domain.Item;
import com.minishop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryMyBatis implements ItemRepository {

    private final ItemMapper itemMapper;

    @Override
    public Item save(Item item) {
        itemMapper.save(item);
        return item;
    }

    @Override
    public Item update(Item item) {
        itemMapper.update(item);
        return item;
    }

    @Override
    public void delete(Long id) {
        itemMapper.delete(id);
    }

    @Override
    public Item findById(Long id) {
        return itemMapper.findById(id);
    }

    @Override
    public List<Item> findAll() {
        return itemMapper.findAll();
    }
}
