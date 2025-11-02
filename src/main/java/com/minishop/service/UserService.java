package com.minishop.service;

import com.minishop.domain.Users;
import com.minishop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Users save(Users user) {
        return userRepository.save(user);
    }

    public void delete(Long id) {
        int deletedRows =  userRepository.delete(id);
        if(deletedRows == 0)
            throw new RuntimeException("삭제할 유저가 없습니다.");
    }

    public Users findById(Long id) {
        return userRepository.findById(id);
    }

    public List<Users> findAll() {
        return userRepository.findAll();
    }


    public int update(Long id, Users user) {
        return userRepository.update(id, user);
    }
}
