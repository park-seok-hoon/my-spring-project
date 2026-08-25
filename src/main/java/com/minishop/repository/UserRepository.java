package com.minishop.repository;

import com.minishop.domain.user.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    User update(Long id, User user);

    int delete(Long id);

    Optional<User> findById(Long id);

    List<User> findAll();

    User findByEmail(String email);

    void deleteAll();
}