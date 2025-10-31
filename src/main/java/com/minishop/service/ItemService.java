package com.minishop.service;

import com.minishop.domain.Item;
import com.minishop.repository.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {

    //인터페이스에 대해서만 알고 있어도 스프링에서 자동으로 해당 구현체로 연결해줌으로 신경을 쓰지 않아도 됨.
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public Item update(Item item) {
        return itemRepository.update(item);
    }

    public void delete(Long id) {
        itemRepository.delete(id);
    }

    public Item findById(Long id) {
        return itemRepository.findById(id);
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }
}
