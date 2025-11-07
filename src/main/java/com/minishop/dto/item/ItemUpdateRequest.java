package com.minishop.dto.item;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ItemUpdateRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    @Min(value = 1, message = "가격은 1원 이상이어야 합니다.")
    private int price;

    @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
    private int stockQuantity;
}
