package com.minishop.repository;

import com.minishop.domain.Items;

import java.util.List;

public interface ItemRepository {
    Items save(Items item);
    int update(Long id, Items items);
    int delete(Long id);
    Items findById(Long id);
    List<Items> findAll();

}
