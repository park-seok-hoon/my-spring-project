package com.minishop.repository.mybatis;


import com.minishop.domain.Users;
import com.minishop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class UserRepositoryMyBatis implements UserRepository {

    private final UserMapper userMapper;

    @Override
    public Users save(Users user) {
        userMapper.insertUser(user);
        return user;
    }

    @Override
    public int update(Long id, Users user) {
        return userMapper.updateUser(id, user);
    }

    @Override
    public int delete(Long id) {
        return userMapper.deleteUser(id);
    }

    @Override
    public Users findById(Long id) {
        return userMapper.findUserById(id);
    }

    @Override
    public List<Users> findAll() {
        return userMapper.findAllUsers();
    }
}
