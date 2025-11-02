package com.minishop.repository.mybatis;

import com.minishop.domain.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    List<Users> findAllUsers(); //READ
    Users findUserById(Long id); //READ
    void insertUser(Users user); //CREATE
    int updateUser(@Param("id")Long id, @Param("user")Users user); //UPDATE
    int deleteUser(Long id); //DELETE
}