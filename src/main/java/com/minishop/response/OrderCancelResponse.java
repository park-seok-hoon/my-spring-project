package com.minishop.response;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class OrderCancelResponse {

    private Long orderId;
    private List<ItemCancelInfo> items;


    @Data
    @AllArgsConstructor
    public static class ItemCancelInfo {
        private Long itemId;
        private String itemName;
        private int beforeQuantity; //취소 전 주문 수량
        private int restoredQuantity; //복구된 수량 (동일)

    }

}
