package com.minishop.repository;

import com.minishop.domain.Items;
import com.minishop.domain.Users;

import java.util.List;

public interface UserRepository {

    Users save(Users user);
    int update(Long id, Users user);
    int delete(Long id);
    Users findById(Long id);
    List<Users> findAll();
}
