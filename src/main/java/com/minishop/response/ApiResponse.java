package com.minishop.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {

    private LocalDateTime timestamp;  // 응답 시간
    private String path;              // 요청 URL
    private String code;              // 성공/에러 코드
    private String message;           // 메시지
    private T data;                   // 성공 시 데이터, 실패 시 null

    // 현재 요청 path 가져오기
    private static String getCurrentPath() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attrs == null) return null;

            HttpServletRequest request = attrs.getRequest();
            return request.getRequestURI();
        } catch (Exception e) {
            return null;
        }
    }

    // ✔ 성공 (기본 메시지)
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .path(getCurrentPath())
                .code("SUCCESS")
                .message("요청이 성공적으로 처리되었습니다.")
                .data(data)
                .build();
    }

    // ✔ 성공 + 메시지
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .path(getCurrentPath())
                .code("SUCCESS")
                .message(message)
                .data(data)
                .build();
    }

    // ✔ 실패 응답
    public static <T> ApiResponse<T> error(String code, String message, String path) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .path(path)
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}
