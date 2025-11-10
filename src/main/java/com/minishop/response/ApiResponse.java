package com.minishop.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String code;    //상태 코드(SUCCESS, ERROR 등)
    private String message; //설명 메시지
    private T data;         //응답 데이터 (성공 시만 포함)

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "요청이 성공적으로 처리되었습니다.",data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS",message,data);

    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code,message,null);
    }

}
