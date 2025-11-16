package com.minishop.repository;

import com.minishop.domain.Items;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Optional<Items> findById(Long id);
    Items save(Items item);
    int update(Long id, Items items);
    int delete(Long id);
    List<Items> findAll();
    Items findByName(String itemName);
    void update(Items dbItem);
    void updateStock(Items dbItem); //재고 업데이트
}
