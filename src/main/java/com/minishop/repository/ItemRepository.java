package com.minishop.repository;

import com.minishop.domain.Item;

import java.util.List;

public interface ItemRepository {
    Item save(Item item);
    Item update(Item item);
    void delete(Long id);
    Item findById(Long id);
    List<Item> findAll();
}
