package com.minishop.service;

import com.minishop.domain.Users;
import com.minishop.dto.item.UserCreateRequest;
import com.minishop.dto.item.UserUpdateRequest;
import com.minishop.exception.AppException;
import com.minishop.exception.ErrorCode;
import com.minishop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void clearDB() {
        userRepository.deleteAll(); // deleteAll() 반드시 만들어둔 메서드
    }

    @Test
    @DisplayName("사용자 등록 성공")
    void saveSuccess() {

        UserCreateRequest req = new UserCreateRequest("박석훈", "password123", "test@example.com");

        Users saved = userService.save(req);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("박석훈");
        assertThat(saved.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("사용자 등록 실패 - 이메일 중복")
    void saveFail_DuplicateEmail() {

        //given
        userService.save(new UserCreateRequest("박석훈", "password123", "dup@email.com"));

        UserCreateRequest req = new UserCreateRequest("박석훈1", "password123", "dup@email.com");

        // when & then
        assertThatThrownBy(() -> userService.save(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("이미 등록된 이메일입니다");
    }

    @Test
    @DisplayName("사용자 등록 실패 - 비밀번호 8자 미만")
    void saveFail_InvalidPassword() {

        UserCreateRequest req = new UserCreateRequest("박석훈", "1234567", "test2@example.com");

        assertThatThrownBy(() -> userService.save(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("비밀번호는 8자 이상이어야 합니다.");
    }

    @Test
    @DisplayName("사용자 삭제 성공")
    void deleteSuccess() {

        Users saved = userService.save(new UserCreateRequest("박석훈", "password123", "delete@test.com"));

        userService.delete(saved.getId());

        assertThatThrownBy(() -> userService.findById(saved.getId()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사용자 삭제 실패 - 존재하지 않는 ID")
    void deleteFail_NotFound() {

        assertThatThrownBy(() -> userService.delete(99999L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("삭제할 유저가 존재하지 않습니다.");
    }

    @Test
    @DisplayName("단일 사용자 조회 성공")
    void findByIdSuccess() {

        Users saved = userService.save(new UserCreateRequest("박석훈", "password123", "find@test.com"));

        Users found = userService.findById(saved.getId());

        assertThat(found.getUsername()).isEqualTo("박석훈");
    }

    @Test
    @DisplayName("단일 사용자 조회 실패 - 존재하지 않는 ID")
    void findByIdFail_NotFound() {

        assertThatThrownBy(() -> userService.findById(99999L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("전체 사용자 조회 성공")
    void findAllSuccess() {

        userService.save(new UserCreateRequest("박석훈", "password123", "a@test.com"));
        userService.save(new UserCreateRequest("박석훈1", "password123", "b@test.com"));

        List<Users> list = userService.findAll();

        assertThat(list).hasSize(2);
    }

    @Test
    @DisplayName("전체 사용자 조회 성공 - 0명인 경우")
    void findAllEmpty() {

        List<Users> list = userService.findAll();

        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("사용자 정보 수정 성공 - 이름, 비밀번호, 이메일 변경")
    void updateSuccess_AllChange() {

        Users saved = userService.save(new UserCreateRequest("박석훈", "password123", "update@test.com"));

        UserUpdateRequest req = new UserUpdateRequest("이름변경", "newpassword123", "newemail@test.com");

        Users updated = userService.update(saved.getId(), req);

        assertThat(updated.getUsername()).isEqualTo("이름변경");
        assertThat(updated.getEmail()).isEqualTo("newemail@test.com");
        assertThat(updated.getPassword()).isEqualTo("newpassword123");
    }

    @Test
    @DisplayName("사용자 정보 수정 성공 - 일부 필드만 수정")
    void updateSuccess_Partial() {

        Users saved = userService.save(new UserCreateRequest(
                "박석훈", "password123", "tjrgns@test.com"));

        //null 값은 변경하지 않음
        UserUpdateRequest req = new UserUpdateRequest(null, "newpassword123", null);

        Users updated = userService.update(saved.getId(), req);

        assertThat(updated.getUsername()).isEqualTo("박석훈");
        assertThat(updated.getEmail()).isEqualTo("tjrgns@test.com");
        assertThat(updated.getPassword()).isEqualTo("newpassword123");
    }

    @Test
    @DisplayName("사용자 수정 실패 - 존재하지 않는 ID")
    void updateFail_NotFound() {

        UserUpdateRequest req = new UserUpdateRequest(
                "변경", "password123", "new@test.com"
        );

        assertThatThrownBy(() -> userService.update(99999L, req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("사용자 수정 실패 - 비밀번호 8자 미만")
    void updateFail_InvalidPassword() {

        Users saved = userService.save(new UserCreateRequest(
                "홍길동", "password123", "short@test.com"
        ));

        UserUpdateRequest req = new UserUpdateRequest(
                "변경", "12345", "short@test.com"
        );

        assertThatThrownBy(() -> userService.update(saved.getId(), req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("비밀번호는 8자 이상이어야 합니다.");
    }

    @Test
    @DisplayName("사용자 수정 실패 - 이메일 중복")
    void updateFail_DuplicateEmail() {

        // User1
        userService.save(new UserCreateRequest(
                "유저1", "password123", "dup1@test.com"
        ));

        // User2
        Users user2 = userService.save(new UserCreateRequest(
                "유저2", "password123", "dup2@test.com"
        ));

        // user2 → user1의 이메일로 변경 요청 → FAIL
        UserUpdateRequest req = new UserUpdateRequest(
                "변경", "password999", "dup1@test.com"
        );

        assertThatThrownBy(() -> userService.update(user2.getId(), req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("이미 등록된 이메일입니다");
    }
}
