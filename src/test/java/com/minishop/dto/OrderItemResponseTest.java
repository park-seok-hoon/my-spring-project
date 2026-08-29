package com.minishop.dto;

import com.minishop.domain.item.Item;
import com.minishop.domain.order.OrderItem;
import com.minishop.dto.order.OrderItemResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemResponseTest {

    @Test
    @DisplayName("OrderItem 엔티티를 OrderItemResponse로 변환하면 item 정보까지 함께 매핑된다")
    void from_mapsNestedItemCorrectly() {
        Item item = new Item();
        ReflectionTestUtils.setField(item, "id", 1L);
        item.setName("운동화");
        item.setPrice(50000);
        item.setStockQuantity(8);

        OrderItem orderItem = new OrderItem();
        ReflectionTestUtils.setField(orderItem, "id", 100L);
        orderItem.setQuantity(2);
        orderItem.setItem(item);

        OrderItemResponse response = OrderItemResponse.from(orderItem);

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.quantity()).isEqualTo(2);
        assertThat(response.item().id()).isEqualTo(1L);
        assertThat(response.item().name()).isEqualTo("운동화");
    }
}