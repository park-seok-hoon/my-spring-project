package com.minishop.controller;


import com.minishop.domain.Items;
import com.minishop.domain.Users;
import com.minishop.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //insert
    @PostMapping
    public ResponseEntity<Users> createUsers(@RequestBody Users user) {

        try{
            Users saveUser = userService.save(user);
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest().path("/{id}")
                    .buildAndExpand(saveUser.getId())
                    .toUri();
            return ResponseEntity.created(location).body(saveUser);
        }catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    //update
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @RequestBody Users user) {
        user.setId(id);
        userService.update(id,user);
        int updateRows = userService.update(id,user);
        //실패 시
        if(updateRows == 0){
            return ResponseEntity.notFound().build();
        }
        //성공 시
        return ResponseEntity.status(HttpStatus.OK).body("회원 정보 변경 완료");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable("id") Long id) {
        try{
            userService.delete(id);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("회원이 삭제 되었습니다."); // 200 OK
        }catch(RuntimeException e) {
            return ResponseEntity.notFound().build();   //실패 204
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id) {
        try {
            Users user = userService.findById(id);
            return ResponseEntity.ok(user); //200 OK
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); //실패 404
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllItems() {
        try{
            List<Users> user = userService.findAll();
            //아무것도 들어있지 않은 경우
            if( user.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body("조회된 회원이 없습니다."); // 200 OK
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("회원 조회 중 오류 발생" + e.getMessage());  //예외 발생 시 500 Internal Server Error
        }
    }




}
