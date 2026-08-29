package com.minishop.service;


import com.minishop.domain.user.User;
import com.minishop.dto.user.UserCreateRequest;
import com.minishop.dto.user.UserUpdateRequest;
import com.minishop.exception.AppException;
import com.minishop.exception.ErrorCode;

import com.minishop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 사용자 등록
     */
    public User save(UserCreateRequest request) {
        // 이메일 중복 검증
        User existingUser = userRepository.findByEmail(request.getEmail());

        if (existingUser != null) {
            throw new AppException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 비즈니스 유효성 검증 (추가 검증 가능)
        if (request.getPassword().length() < 8) {
            throw new AppException(ErrorCode.INVALID_USER_DATA, "비밀번호는 8자 이상이어야 합니다.");
        }

        // DTO → Entity 변환
        User user = new User();
        user.changeUsername(request.getUsername());
        user.changePassword(request.getPassword());
        user.changeEmail(request.getEmail());

        return userRepository.save(user);
    }

    /**
     * 사용자 삭제
     */
    @Transactional
    public void delete(Long id) {
        int deletedRows = userRepository.delete(id);
        if (deletedRows == 0) {
            throw new AppException(ErrorCode.USER_NOT_FOUND, "삭제할 유저가 존재하지 않습니다.");
        }
    }

    /**
     * 단일 사용자 조회
     */
    public User findById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        return user.orElse(null);
    }

    /**
     * 전체 사용자 조회
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * 사용자 정보 수정
     */
    @Transactional
    public User update(Long id, UserUpdateRequest request) {

        // 기존 사용자 존재 여부 확인
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        // 이메일 형식 검증은 @Valid에서 이미 수행됨
        // 이곳에서는 비즈니스 검증만 수행

        // 변경된 필드만 업데이트
        if (request.getUsername() != null) {
            existingUser.get().changeUsername(request.getUsername());
        }

        if (request.getPassword() != null && request.getPassword().length() < 8) {
            throw new AppException(ErrorCode.INVALID_USER_DATA, "비밀번호는 8자 이상이어야 합니다.");
        }

        if (request.getPassword() != null) {
            existingUser.get().changePassword(request.getPassword());
        }

        if (request.getEmail() != null) {
            User duplicate = userRepository.findByEmail(request.getEmail());

            // 이메일이 존재하고, 그게 "나 자신이 아닌 다른 사용자"라면 중복 예외
            if (duplicate != null && !Objects.equals(duplicate.getId(), id)) {
                throw new AppException(ErrorCode.DUPLICATE_EMAIL);
            }

            // 그렇지 않으면 (즉, 내 이메일 그대로거나 새 이메일이면) 정상 처리
            existingUser.get().changeEmail(request.getEmail());
        }

        return userRepository.update(id, existingUser.orElse(null));
    }
}