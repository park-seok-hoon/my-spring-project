package com.minishop.controller;


import com.minishop.domain.Items;
import com.minishop.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    //insert
    @PostMapping
    public ResponseEntity<Items> createItem(@RequestBody Items item) {
        try{
            Items savedItem =itemService.save(item);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(savedItem.getId())
                    .toUri();
            return ResponseEntity.created(location).body(savedItem);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //예외
    @GetMapping("/error-ex")
    public void errorEx() {
        throw new RuntimeException("예외 발생!");
    }

    //update
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable("id") Long id, @RequestBody Items items) {
        items.setId(id);
        itemService.update(id,items);
        int updateRows = itemService.update(id,items);
        log.info("바뀐 행의 수= {}", updateRows);

        //실패 시
        if(updateRows == 0){
            return ResponseEntity.notFound().build();
        }

        //성공 시
        return ResponseEntity.status(HttpStatus.OK).body("아이템 변경 완료");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable("id") Long id) {
        try{
           itemService.delete(id);
           return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("상품이 삭제되었습니다."); // 200 OK
        }catch(RuntimeException e) {
            return ResponseEntity.notFound().build();   //실패 204
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id) {
       try {
           Items item = itemService.findById(id);
           return ResponseEntity.ok(item); //200 OK
       } catch (IllegalArgumentException e) {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); //실패 404
       }
    }

    @GetMapping
    public ResponseEntity<?> getAllItems() {
        try{
            List<Items> items = itemService.findAll();
            //아무것도 들어있지 않은 경우
            if( items.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body("조회된 상품이 없습니다."); // 200 OK
            }
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("상품 목록 조회 중 오류 발생" + e.getMessage());  //예외 발생 시 500 Internal Server Error
        }
    }




}
