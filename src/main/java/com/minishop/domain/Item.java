package com.minishop.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {

    private Long id;

    private String name;
    private int price;
    private int stockQuantity; //재고 수량
}
