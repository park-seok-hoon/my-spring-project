package com.minishop.repository;

import com.minishop.domain.Items;

import java.util.List;

public interface ItemRepository {
    Items save(Items item);
    int update(Long id, Items items);
    void delete(Long id);
    Items findById(Long id);
    List<Items> findAll();

}
