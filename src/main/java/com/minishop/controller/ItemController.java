package com.minishop.controller;


import com.minishop.domain.Items;
import com.minishop.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public Items create(@RequestBody Items item) {
        return itemService.save(item);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable("id") Long id, @RequestBody Items items) {
        items.setId(id);
        itemService.update(id,items);
        int updateRows = itemService.update(id,items);
        log.info("바뀐 행의 수= {}", updateRows);

        //실패 시
        if(updateRows == 0){
            return ResponseEntity.notFound().build();
        }

        //성공 시
        return ResponseEntity.noContent().build();

    }



}
