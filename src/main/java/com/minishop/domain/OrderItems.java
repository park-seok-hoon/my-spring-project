package com.minishop.domain;

import lombok.Data;

@Data
public class OrderItems {
    private Long id;       // PK
    private Long orderId;  // 주문 ID (Orders FK)
    private Long itemId;   // 상품 ID (Items FK)
    private int quantity;  // 수량

    private Items item; //주문한 상품 정보 자세히 1:1 관계
}
