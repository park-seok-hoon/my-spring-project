package com.minishop.controller;

import com.minishop.domain.Users;
import com.minishop.dto.item.UserCreateRequest;
import com.minishop.dto.item.UserUpdateRequest;
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
    public ResponseEntity<ApiResponse<Users>> createUsers(@Valid @RequestBody UserCreateRequest request) {
        Users user = userService.save(request);
        return ResponseEntity.ok(ApiResponse.success("유저 등록 성공", user));
    }

    /**
     * ✅ 유저 전체 조회 (Read All)
     * 상품이 없으면 AppException에서 user_NOT_FOUND 발생
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Users>>> getAllUsers() {
        List<Users> users = userService.findAll();
        return ResponseEntity.ok(ApiResponse.success("유저 목록 조회 성공",users));
    }

    /**
     * ✅ 유저 한명 조회 (Read One)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Users>> getById(@PathVariable("id") Long id) {
        Users findUser = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("유저 목록 조회 성공",findUser));
    }

    /**
     * ✅ 상품 수정 (Update)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Users>> updateUser(@PathVariable("id") Long id,@Valid @RequestBody UserUpdateRequest updateRequest) {
        System.out.println("✅ PUT 요청 들어옴: id = " + id);
        Users updateUser = userService.update(id, updateRequest);
        return ResponseEntity.ok(ApiResponse.success("유저 정보 수정 성공.",updateUser));

    }

    /**
     * ✅ 상품 삭제 (Delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Users>> deleteItem(@PathVariable("id") Long id) {
        userService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("유저 삭제 성공.",null));
    }

    /**
     * ✅ 테스트용 예외 (임의 호출)
     */
    @GetMapping("/error-ex")
    public void errorEx() {
        throw new RuntimeException("테스트용 예외 발생!");
    }

}
