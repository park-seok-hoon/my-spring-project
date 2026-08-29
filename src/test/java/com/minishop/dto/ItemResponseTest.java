package com.minishop.dto;

import com.minishop.domain.item.Item;
import com.minishop.dto.item.ItemResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ItemResponseTest {

    @Test
    @DisplayName("Item 엔티티를 ItemResponse로 변환하면 필드가 정확히 매핑된다")
    void from_mapsFieldsCorrectly() {
        // given
        Item item = new Item();
        ReflectionTestUtils.setField(item, "id", 1L);
        item.setName("운동화");
        item.setPrice(50000);
        item.setStockQuantity(10);

        // when
        ItemResponse response = ItemResponse.from(item);

        // then
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("운동화");
        assertThat(response.price()).isEqualTo(50000);
        assertThat(response.stockQuantity()).isEqualTo(10);
    }
}