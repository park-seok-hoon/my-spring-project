package com.minishop.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Orders {
    private Long id;                // 주문 ID   (Orders PK)
    private Long userId;            // 주문자 ID (Users FK)
    private LocalDateTime orderDate;// 주문일
    private int totalPrice;         // 총 가격
    private String status; // NEW(생성), CANCELLED(취소) , SHIPPED , COMPLETED

    private List<OrderItems> orderItems; // 주문 상품 목록  1:N 관계  1개의 주문에 여러개의 상품들을 주문 가능
}