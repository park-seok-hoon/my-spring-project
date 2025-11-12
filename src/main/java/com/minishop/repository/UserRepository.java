package com.minishop.repository;

import com.minishop.domain.Users;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserRepository {

    Users save(Users user);
    Users update(Long id, Users user);
    int delete(Long id);
    Users findById(Long id);
    List<Users> findAll();
    Users findByEmail(@Param("email") String email);
}
