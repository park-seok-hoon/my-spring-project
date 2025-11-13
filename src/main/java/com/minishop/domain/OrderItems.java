package com.minishop.domain;

import lombok.Data;

@Data
public class OrderItems {

    private Long orderId;  // 주문 ID (Orders FK)
    private Long itemId;   // 상품 ID (Items FK)
    private int quantity;  // 수량
}
