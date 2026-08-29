package com.minishop.dto.item;


import com.minishop.domain.item.Item;

public record ItemResponse(
        Long id,
        String name,
        int price,
        int stockQuantity
) {
    public static ItemResponse from(Item item) {
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getPrice(),
                item.getStockQuantity());
    }
}
