package com.minishop.service;

import com.minishop.domain.Items;
import com.minishop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public Items save(Items item) {
        return itemRepository.save(item);
    }

    public Items findById(Long id) {
        return itemRepository.findById(id);
    }

    public List<Items> findAll() {
        return itemRepository.findAll();
    }

    public int update(Long id, Items item) {
        return itemRepository.update(id, item);
    }

    public int delete(Long id) {
        return itemRepository.delete(id);
    }
}
