package com.minishop.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class OrderModifyRequest {

    //수정할 주문상품들 목록
    private List<OrderModifyItem> items;

    //주문 내에서 한 줄(order_items 한 row)를 수정할 때 사용하는 DTO
    @Data
    @AllArgsConstructor
    public static class OrderModifyItem {
        private Long orderItemId; // 기존 주문상품 ID
        private Long itemId;      // 새 상품 ID (기존과 같을 수도 있음)
        private int quantity;     // 새 수량


    }

}
