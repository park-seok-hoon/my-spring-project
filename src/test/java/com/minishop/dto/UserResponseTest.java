package com.minishop.dto.user;

import com.minishop.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseTest {

    @Test
    @DisplayName("User 엔티티를 UserResponse로 변환하면 id, username, email만 매핑된다")
    void from_mapsFieldsCorrectly() {
        User user = User.builder()
                .username("홍길동")
                .password("password123")
                .email("test@example.com")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        UserResponse response = UserResponse.from(user);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("홍길동");
        assertThat(response.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("[보안] UserResponse에는 password 관련 필드가 절대 존재하면 안 된다")
    void mustNotContainPasswordField() {
        boolean hasPasswordField = Arrays.stream(UserResponse.class.getDeclaredFields())
                .anyMatch(field -> field.getName().toLowerCase().contains("password"));

        assertThat(hasPasswordField)
                .as("UserResponse는 password 관련 필드를 포함해서는 안 됩니다.")
                .isFalse();
    }
}