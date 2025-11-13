package com.minishop.dto.order;

import lombok.Data;

@Data
public class OrderUpdateRequest {
    private Long userId;     // 주문자 변경
    private String status;   // 주문 상태 변경 (NEW, CANCELLED 등)
}