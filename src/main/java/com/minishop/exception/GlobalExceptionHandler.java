package com.minishop.exception;


import com.minishop.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn("[AppException] code={}, message={}", errorCode.name(), errorCode.getMessage());

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode.name(), e.getMessage()));
    }

    // 그 외 모든 예외 (시스템 에러)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResult> handleException(Exception e) {
        log.error("[Exception] {}", e.getMessage(), e);
        ErrorResult error = new ErrorResult("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
        return ResponseEntity.internalServerError().body(error);
    }


    //@Valid 검증에서 실패 했을 경우에 발생하는 예외를 잡아서 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResult> handleValidationExceptions(MethodArgumentNotValidException e) {

        // 첫 번째 필드 에러 메시지 추출
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다.");

        log.warn("[ValidationException] {}", errorMessage);


        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        for (FieldError fe : fieldErrors) {
            log.warn("필드 에러 표시 field={}, message={}", fe.getField(), fe.getDefaultMessage());
        }

        // 응답 DTO 생성
        ErrorResult error = new ErrorResult(
                "BAD_REQUEST",
                errorMessage
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }



}
