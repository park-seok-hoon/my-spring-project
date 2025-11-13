// dto/order/OrderCreateRequest.java
package com.minishop.dto.order;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrderCreateRequest {
    private Long userId;

    // 🔥 null 방지: 기본값 초기화
    private List<OrderItemRequest> orderItems = new ArrayList<>(); // 상품 목록
}
