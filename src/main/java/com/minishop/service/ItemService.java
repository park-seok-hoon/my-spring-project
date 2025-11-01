package com.minishop.service;

import com.minishop.domain.Items;
import com.minishop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    //인터페이스에 대해서만 알고 있어도 스프링에서 자동으로 해당 구현체로 연결해줌으로 신경을 쓰지 않아도 됨.
    private final ItemRepository itemRepository;

    public Items save(Items item) {
        return itemRepository.save(item);
    }



    public void delete(Long id) {
        itemRepository.delete(id);
    }

    public Items findById(Long id) {
        return itemRepository.findById(id);
    }

    public List<Items> findAll() {
        return itemRepository.findAll();
    }


    public int update(Long id, Items items) {
        return itemRepository.update(id, items);
    }
}
