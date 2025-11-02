package com.minishop.repository.mybatis;

import com.minishop.domain.Items;
import com.minishop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryMyBatis implements ItemRepository {

    private final ItemMapper itemMapper;

    @Override
    public Items save(Items item) {
        itemMapper.saveItem(item);
        return item;
    }

    @Override
    public int update(Long id, Items items) {
        return itemMapper.updateItem(id,items);
    }

    @Override
    public int delete(Long id) {
        return itemMapper.deleteItem(id);
    }

    @Override
    public Items findById(Long id) {
        return itemMapper.findById(id);
    }

    @Override
    public List<Items> findAll() {
        return itemMapper.findAll();
    }


}
