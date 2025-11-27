package com.minishop.dto.item;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class UserUpdateRequest {

    @Size(min = 2, max = 20, message = "사용자 이름은 2자 이상 20자 이하로 입력해야 합니다.")
    private String username;

    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해야 합니다.")
    private String password;

    @Email(message = "유효한 이메일 주소 형식이 아닙니다.")
    private String email;


}
