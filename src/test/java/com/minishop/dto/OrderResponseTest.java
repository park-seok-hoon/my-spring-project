package com.minishop.dto.order;

import com.minishop.domain.item.Item;
import com.minishop.domain.order.Order;
import com.minishop.domain.order.OrderItem;
import com.minishop.domain.order.OrderStatus;
import com.minishop.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderResponseTest {

    @Test
    @DisplayName("Order 엔티티를 OrderResponse로 변환하면 user는 id만, orderItems는 전체가 매핑된다")
    void from_mapsOrderGraphCorrectly() {
        User user = User.builder()
                .username("박석훈")
                .password("password123")
                .email("test@example.com")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        Item item = new Item();
        ReflectionTestUtils.setField(item, "id", 10L);
        item.setName("운동화");
        item.setPrice(50000);
        item.setStockQuantity(8);

        OrderItem orderItem = new OrderItem();
        ReflectionTestUtils.setField(orderItem, "id", 100L);
        orderItem.setQuantity(2);
        orderItem.setItem(item);

        Order order = new Order();
        ReflectionTestUtils.setField(order, "id", 1000L);
        order.setUser(user);
        order.setOrderItems(List.of(orderItem));
        order.setOrderDate(LocalDateTime.of(2026, 1, 1, 12, 0));
        order.setTotalPrice(100000);
        order.setOrderStatus(OrderStatus.NEW);

        OrderResponse response = OrderResponse.from(order);

        assertThat(response.id()).isEqualTo(1000L);
        assertThat(response.userId()).isEqualTo(1L); // User 객체 통째로가 아니라 id만
        assertThat(response.totalPrice()).isEqualTo(100000);
        assertThat(response.status()).isEqualTo(OrderStatus.NEW);
        assertThat(response.orderItems()).hasSize(1);
        assertThat(response.orderItems().get(0).item().name()).isEqualTo("운동화");
    }
}