package com.minishop.controller;

import com.minishop.domain.user.User;
import com.minishop.dto.user.UserCreateRequest;
import com.minishop.dto.user.UserResponse;
import com.minishop.dto.user.UserUpdateRequest;
import com.minishop.response.ApiResponse;

import com.minishop.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    /**
     * ✅ 유저 등록 (Create)
     * 예외는 UserService에서 AppException으로 던지고,
     * GlobalExceptionHandler에서 처리됨.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUsers(@Valid @RequestBody UserCreateRequest request) {
        User user = userService.save(request);
        return ResponseEntity.ok(ApiResponse.success("유저 등록 성공", UserResponse.from(user)));
    }

    /**
     * ✅ 유저 전체 조회 (Read All)
     * 상품이 없으면 AppException에서 user_NOT_FOUND 발생
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<User> users = userService.findAll();
        List<UserResponse> userResponses = users.stream().map(UserResponse::from).toList();
        return ResponseEntity.ok(ApiResponse.success("유저 목록 조회 성공", userResponses));
    }

    /**
     * ✅ 유저 한명 조회 (Read One)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(@PathVariable("id") Long id) {
        User findUser = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("유저 목록 조회 성공", UserResponse.from(findUser)));
    }

    /**
     * ✅ 상품 수정 (Update)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@PathVariable("id") Long id, @Valid @RequestBody UserUpdateRequest updateRequest) {
        System.out.println("✅ PUT 요청 들어옴: id = " + id);
        User updateUser = userService.update(id, updateRequest);
        return ResponseEntity.ok(ApiResponse.success("유저 정보 수정 성공.", UserResponse.from(updateUser)));

    }

    /**
     * ✅ 상품 삭제 (Delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> deleteUser(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("유저 삭제 성공.", null));
    }

}