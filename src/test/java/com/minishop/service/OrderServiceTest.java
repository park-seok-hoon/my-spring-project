package com.minishop.service;

import com.minishop.domain.Items;
import com.minishop.domain.Orders;
import com.minishop.dto.order.OrderCreateRequest;
import com.minishop.dto.order.OrderItemRequest;
import com.minishop.repository.ItemRepository;
import com.minishop.repository.OrderRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    ItemRepository itemRepository;

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    OrderService orderService;

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_success() {

        // given - 주문 요청 생성
        OrderCreateRequest request = new OrderCreateRequest();
        request.setUserId(1L);

        OrderItemRequest orderItem = new OrderItemRequest();
        orderItem.setItemId(10L);
        orderItem.setQuantity(2);
        request.setOrderItems(List.of(orderItem));

        System.out.println(request);

        // given - 상품(Mock)
        Items item = new Items();
        item.setId(10L);
        item.setName("상품 테스트");
        item.setPrice(10000);
        item.setStockQuantity(10);

        System.out.println(item);


        // itemRepository.findById(10L) 호출 시 item 반환
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        System.out.println(itemRepository.findById(10L));

        // 주문 저장 void 메서드 Mock 처리
        doNothing().when(orderRepository).save(any(Orders.class));

        // when
        Orders result = orderService.createOrder(request);
        System.out.println(result);

        // then
        assertEquals("NEW", result.getStatus());  // 상태가 NEW인지
        assertEquals(8, item.getStockQuantity()); // 10 - 2 = 8인지
    }
}
