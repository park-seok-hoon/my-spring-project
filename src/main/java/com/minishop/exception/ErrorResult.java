package com.minishop.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResult {

    private LocalDateTime timestamp;  // 에러 발생 시간
    private String code;              // ErrorCode 이름
    private String message;           // 에러 메시지
    private String path;              // 요청 URL
    private Object data;              // 실패 시 null (성공일 때만 채움)
}
