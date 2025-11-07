package com.minishop.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    DUPLICATE_ITEM(HttpStatus.CONFLICT, "동일한 상품이 이미 존재합니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "가격이 올바르지 않습니다."),
    INVALID_STOCK(HttpStatus.BAD_REQUEST, "재고 수량이 유효하지 않습니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }


}
