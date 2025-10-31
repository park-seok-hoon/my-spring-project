package com.minishop.mini_shop.domain;

import lombok.Data;

@Data
public class Item {

    private Long id;
    private String name;
    private int price;
    private int stockQuantity;
}
