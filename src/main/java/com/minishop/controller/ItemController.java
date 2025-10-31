package com.minishop.controller;


import com.minishop.domain.Item;
import com.minishop.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static ItemService itemService;

    @PostMapping
    public Item create(@RequestBody Item item) {
        return itemService.save(item);
    }


}
