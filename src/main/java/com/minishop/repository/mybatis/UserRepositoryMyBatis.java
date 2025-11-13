package com.minishop.repository.mybatis;

import com.minishop.domain.Users;
import com.minishop.exception.AppException;
import com.minishop.exception.ErrorCode;
import com.minishop.repository.UserRepository;
import com.minishop.repository.mybatis.mapper.UserMapper;
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
    public Users update(Long id, Users user) {
        int result = userMapper.updateUser(id, user);

        if (result == 0) {
            throw new AppException(ErrorCode.USER_NOT_FOUND, "업데이트 대상이 존재하지 않습니다.");
        }

        // ✅ DB에서 수정된 최신 데이터 다시 조회해서 반환
        return userMapper.findUserById(id);
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

    @Override
    public Users findByEmail(String email) {
        return userMapper.findUserByEmail(email);
    }
}
