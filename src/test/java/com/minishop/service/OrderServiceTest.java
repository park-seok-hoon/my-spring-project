package com.minishop.service;


import com.minishop.domain.Items;
import com.minishop.domain.Orders;
import com.minishop.dto.order.OrderCreateRequest;
import com.minishop.dto.order.OrderItemRequest;
import com.minishop.dto.order.OrderModifyRequest;
import com.minishop.exception.AppException;
import com.minishop.repository.ItemRepository;
import com.minishop.repository.OrderRepository;
import com.minishop.response.OrderCancelResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    OrderService orderService;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    OrderRepository orderRepository;

    @BeforeEach
    void clearDB() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("주문 생성 - 성공 테스트")
    void CreateOrder() {
        // given
        Items item1 = new Items(null, "운동화", 50000, 10);
        Items item2 = new Items(null, "모자", 15000, 20);


        itemRepository.save(item1);
        itemRepository.save(item2);

        OrderCreateRequest req = new OrderCreateRequest();


        req.setUserId(1L);
        req.setOrderItems(List.of(
                new OrderItemRequest(item1.getId(), 2),
                new OrderItemRequest(item2.getId(), 1)
        ));

        // when
        Orders order = orderService.createOrder(req);

        // then
        //NULL 체크
        assertThat(order.getId()).isNotNull();
        //주문한 주문 아이템의 개수 체크
        assertThat(order.getOrderItems()).hasSize(2);
        //주문 아이템의 가격 체크
        assertThat(order.getTotalPrice()).isEqualTo(50000*2 + 15000);

    }


    @Test
    @DisplayName("주문 수정 - 상품 수량 변경 - 성공 테스트")
    void modifyOrderSuccess() {

        //given

        // 1) 초기 상품 저장
        Items item1 = new Items(null, "운동화", 50000, 10);
        Items item2 = new Items(null, "모자", 15000, 20);

        itemRepository.save(item1);
        itemRepository.save(item2);

        // 2) 주문 생성
        OrderCreateRequest req = new OrderCreateRequest();
        req.setUserId(1L);
        req.setOrderItems(List.of(
                new OrderItemRequest(item1.getId(), 2),
                new OrderItemRequest(item2.getId(), 1)
        ));

        Orders order = orderService.createOrder(req);

        // 3) 수정 요청 생성: 운동화 2→5, 모자 1→3
        OrderModifyRequest modifyReq = new OrderModifyRequest();
        modifyReq.setItems(List.of(
                new OrderModifyRequest.OrderModifyItem(
                        order.getOrderItems().get(0).getId(), item1.getId(), 5),
                new OrderModifyRequest.OrderModifyItem(
                        order.getOrderItems().get(1).getId(), item2.getId(), 3)
        ));

        // when
        Orders updated = orderService.modifyOrder(order.getId(), modifyReq);

        // then
        //수량 체크
        assertThat(updated.getTotalPrice()).isEqualTo(50000 * 5 + 15000 * 3);

        //첫번째 주문 수정한 것의 수량 체크
        assertThat(updated.getOrderItems().get(0).getQuantity()).isEqualTo(5);

        //두번째 주문 수정한 것의 수량 체크
        assertThat(updated.getOrderItems().get(1).getQuantity()).isEqualTo(3);
    }

    @Test
    @DisplayName("주문 수정 후 totalPrice 변경 검증")
    void modifyOrder_Check_TotalPriceChanged() {

        Items item = new Items(null, "운동화", 10000, 10);
        itemRepository.save(item);

        Orders order = orderService.createOrder(
                new OrderCreateRequest(1L, List.of(new OrderItemRequest(item.getId(), 1)))
        );

        int before = order.getTotalPrice(); // 10000

        OrderModifyRequest req = new OrderModifyRequest();
        req.setItems(List.of(
                new OrderModifyRequest.OrderModifyItem(order.getOrderItems().get(0).getId(),
                        item.getId(), 5)
        ));

        Orders updated = orderService.modifyOrder(order.getId(), req);

        assertThat(updated.getTotalPrice()).isEqualTo(10000 * 5);
    }

    @Test
    @DisplayName("전체 주문 조회 - 성공 테스트")
    void findAllOrdersSuccess() {

        Items item = new Items(null, "운동화", 50000, 10);
        itemRepository.save(item);

        orderService.createOrder(new OrderCreateRequest(
                1L, List.of(new OrderItemRequest(item.getId(), 2))
        ));

        List<Orders> orders = orderRepository.findAll();

        assertThat(orders.size()).isEqualTo(1);
        assertThat(orders.get(0)).isNotNull();

    }

    @Test
    @DisplayName("주문 단건 조회 - 성공 테스트")
    void findOrderSuccess() {

        // given
        Items item1 = new Items(null, "운동화", 50000, 10);
        Items item2 = new Items(null, "모자", 15000, 20);

        itemRepository.save(item1);
        itemRepository.save(item2);

        OrderCreateRequest req = new OrderCreateRequest();
        req.setUserId(1L);
        req.setOrderItems(List.of(
                new OrderItemRequest(item1.getId(), 2),
                new OrderItemRequest(item2.getId(), 1)
        ));

        Orders saved = orderService.createOrder(req);

        System.out.println(saved);
        // when
        Orders found = orderService.findOrder(saved.getId());

        System.out.println(found);
        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getOrderItems()).hasSize(2);
        assertThat(found.getTotalPrice()).isEqualTo(50000 * 2 + 15000);
    }

    @Test
    @DisplayName("주문 취소 - 성공 테스트 (재고 복구 + 상태 변경 + 응답 검증)")
    void cancelOrderSuccess() {

        //given
        Items item1 = new Items(null, "운동화", 50000, 10);
        Items item2 = new Items(null, "모자", 15000, 20);

        itemRepository.save(item1);
        itemRepository.save(item2);

        // 주문 요청 생성 (운동화 2개, 모자 1개)
        OrderCreateRequest req = new OrderCreateRequest();
        req.setUserId(1L);
        req.setOrderItems(List.of(
                new OrderItemRequest(item1.getId(), 2),
                new OrderItemRequest(item2.getId(), 1)
        ));


        // when
        Orders order = orderService.createOrder(req);

        // db는 재고가 변화가 됐지만 item1,item2는 재고 변화가 되지 않았음 db 재고 8, 19   item1,item2 재고 10 20인 상태
        Items dbItem1 = itemRepository.findById(item1.getId()).orElseThrow();
        Items dbItem2 = itemRepository.findById(item2.getId()).orElseThrow();

        int beforeStock1 = dbItem1.getStockQuantity();  // 예상: 8
        int beforeStock2 = dbItem2.getStockQuantity();  // 예상: 19

        //아이템 재고 감소 확인
        assertThat(beforeStock1).isEqualTo(8);
        assertThat(beforeStock2).isEqualTo(19);


        OrderCancelResponse response = orderService.cancelOrder(order.getId());

        System.out.println(response);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(order.getId());
        assertThat(response.getItems()).hasSize(2);

        // 응답 안에서 각 상품의 복구 정보 확인
        OrderCancelResponse.ItemCancelInfo i1 = response.getItems().get(0);
        OrderCancelResponse.ItemCancelInfo i2 = response.getItems().get(1);

        // 복구된 수량이 맞는지 검증 8->10 , 19->20
        assertThat(i1.getRestoredQuantity()).isEqualTo(10);
        assertThat(i2.getRestoredQuantity()).isEqualTo(20);


        // 5. THEN: 실제 DB 재고가 복구되었는지 검증
        Items afterItem1 = itemRepository.findById(item1.getId()).orElseThrow();
        Items afterItem2 = itemRepository.findById(item2.getId()).orElseThrow();

        assertThat(afterItem1.getStockQuantity()).isEqualTo(beforeStock1 + 2); // 8 + 2 = 10
        assertThat(afterItem2.getStockQuantity()).isEqualTo(beforeStock2 + 1); // 19 + 1 = 20


        // 6. THEN: 주문 상태가 CANCELLED인지 검증
        Orders cancelledOrder = orderRepository.findById(order.getId());
        assertThat(cancelledOrder.getStatus()).isEqualTo("CANCELLED");
    }

    //주문 생성 실패 검증
    @Test
    @DisplayName("주문 생성 실패 - orderItems가 null 또는 비어있음")
    void createOrder_Fail_InvalidRequest() {

        OrderCreateRequest req = new OrderCreateRequest();
        req.setUserId(1L);
        req.setOrderItems(null); // 또는 new ArrayList<>()

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("잘못된 요청");
    }

    @Test
    @DisplayName("주문 생성 실패 - 존재하지 않는 상품(itemId)")
    void createOrder_Fail_ItemNotFound() {

        OrderCreateRequest req = new OrderCreateRequest();
        req.setUserId(1L);
        req.setOrderItems(List.of(
                new OrderItemRequest(99999L, 2)  // 존재하지 않는 상품 ID
        ));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("주문 생성 실패 - 상품 가격 음수")
    void createOrder_Fail_InvalidPrice() {

        Items item = new Items(null, "운동화", -1000, 10);
        itemRepository.save(item);

        OrderCreateRequest req = new OrderCreateRequest();
        req.setUserId(1L);
        req.setOrderItems(List.of(
                new OrderItemRequest(item.getId(), 2)
        ));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("가격이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("주문 생성 실패 - 주문 수량이 0 이하")
    void createOrder_Fail_InvalidQuantity() {

        Items item = new Items(null, "운동화", 10000, 10);
        itemRepository.save(item);

        OrderCreateRequest req = new OrderCreateRequest();
        req.setUserId(1L);
        req.setOrderItems(List.of(
                new OrderItemRequest(item.getId(), 0)  // 0개 주문
        ));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("주문 수량은 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("주문 생성 실패 - 재고 부족")
    void createOrder_Fail_OutOfStock() {

        Items item = new Items(null, "운동화", 10000, 3); // 재고 3
        itemRepository.save(item);

        OrderCreateRequest req = new OrderCreateRequest();
        req.setUserId(1L);
        req.setOrderItems(List.of(
                new OrderItemRequest(item.getId(), 10)  // 주문 수량 10
        ));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("재고가 부족합니다");
    }

    @Test
    @DisplayName("주문 생성 실패 - 가격 계산 결과 int 범위 초과")
    void createOrder_Fail_PriceOverflow() {

        Items item = new Items(null, "초고가 상품", Integer.MAX_VALUE, 10);
        itemRepository.save(item);

        OrderCreateRequest req = new OrderCreateRequest();
        req.setUserId(1L);
        req.setOrderItems(List.of(
                new OrderItemRequest(item.getId(), 2)
                // Integer.MAX_VALUE * 2 → overflow
        ));

        assertThatThrownBy(() -> orderService.createOrder(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("상품 금액 계산 중 오류가 발생했습니다.");
    }


    //주문 수정 실패 검증

    @Test
    @DisplayName("주문 수정 실패 - 요청 items가 null 또는 empty")
    void modifyOrder_Fail_InvalidRequest() {
        // 해당 코드 검증
        // if (request.getItems() == null || request.getItems().isEmpty()) {
        //           throw new AppException(ErrorCode.INVALID_REQUEST);
        //        }

        OrderModifyRequest req = new OrderModifyRequest();
        req.setItems(null); // empty도 테스트 가능

        assertThatThrownBy(() -> orderService.modifyOrder(1L, req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("잘못된 요청");
    }


    @Test
    @DisplayName("주문 수정 실패 - 주문을 찾을 수 없음")
    void modifyOrder_Fail_OrderNotFound() {
        // 해당 코드 검증
        //Orders order = orderRepository.findById(orderId);
        //if (order == null) {
        //    throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        //}

        OrderModifyRequest req = new OrderModifyRequest();
        req.setItems(List.of(
                new OrderModifyRequest.OrderModifyItem(1L, 1L, 5)
        ));

        assertThatThrownBy(() -> orderService.modifyOrder(99999L, req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("주문을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("주문 수정 실패 - 주문 상태가 COMPLETED인 경우")
    void modifyOrder_Fail_CannotModifyCompleted() {
        // 해당 코드 검증
        //if ("COMPLETED".equals(order.getStatus())) {
        //    throw new AppException(ErrorCode.CANNOT_MODIFY_COMPLETED);
        //}

        // given
        Items item = new Items(null, "운동화", 50000, 10);
        itemRepository.save(item);

        OrderCreateRequest req = new OrderCreateRequest();
        req.setUserId(1L);
        req.setOrderItems(List.of(new OrderItemRequest(item.getId(), 2)));

        Orders order = orderService.createOrder(req);

        // 상태를 Completed 로 강제 업데이트
        orderRepository.updateStatus(order.getId(), "COMPLETED");

        // when
        OrderModifyRequest modifyReq = new OrderModifyRequest();
        modifyReq.setItems(List.of(
                new OrderModifyRequest.OrderModifyItem(
                        order.getOrderItems().get(0).getId(),
                        item.getId(),
                        5
                )
        ));

        assertThatThrownBy(() -> orderService.modifyOrder(order.getId(), modifyReq))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("수정할 수 없습니다");
    }

    @Test
    @DisplayName("주문 수정 실패 - 기존 주문 상세(orderItems)가 없음")
    void modifyOrder_Fail_OrderItemsNotFound() {
    // 해당 코드 검증
    //        List<OrderItems> oldItems = order.getOrderItems();
    //        if (oldItems == null || oldItems.isEmpty()) {
    //            throw new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND);
    //        }


        // given
        Orders order = new Orders();
        order.setUserId(1L);
        order.setStatus("NEW");
        order.setTotalPrice(0);
        order.setOrderItems(new ArrayList<>());
        orderRepository.save(order);

        OrderModifyRequest req = new OrderModifyRequest();
        req.setItems(List.of(
                new OrderModifyRequest.OrderModifyItem(1L, 1L, 3)
        ));

        assertThatThrownBy(() -> orderService.modifyOrder(order.getId(), req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("주문 상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("주문 수정 실패 - 수정하려는 orderItemId가 존재하지 않음")
    void modifyOrder_Fail_OrderItemNotFound() {
    // 해당 코드 검증
    //        OrderItems old = oldMap.get(reqItem.getOrderItemId());
    //        if (old == null) {
    //            throw new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND);
    //        }


        Items item = new Items(null, "운동화", 50000, 10);
        itemRepository.save(item);

        // 주문 생성
        OrderCreateRequest req = new OrderCreateRequest();
        req.setUserId(1L);
        req.setOrderItems(List.of(new OrderItemRequest(item.getId(), 2)));
        Orders order = orderService.createOrder(req);

        // 존재하지 않는 orderItemId로 수정
        OrderModifyRequest modifyReq = new OrderModifyRequest();
        modifyReq.setItems(List.of(
                new OrderModifyRequest.OrderModifyItem(99999L, item.getId(), 10)
        ));

        assertThatThrownBy(() -> orderService.modifyOrder(order.getId(), modifyReq))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("주문 상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("주문 수정 실패 - 기존 상품(oldProduct)이 존재하지 않음")
    void modifyOrder_Fail_OldProductNotFound() {

        // 해당 코드 검증
        //Items oldProduct = itemRepository.findById(old.getItemId())
        //        .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        Items item = new Items(null, "운동화", 50000, 10);
        itemRepository.save(item);

        Orders order = orderService.createOrder(
                new OrderCreateRequest(1L, List.of(new OrderItemRequest(item.getId(), 2)))
        );

        // 기존 상품 삭제 (강제 상황)
        itemRepository.delete(item.getId());

        OrderModifyRequest modifyReq = new OrderModifyRequest();
        modifyReq.setItems(List.of(
                new OrderModifyRequest.OrderModifyItem(order.getOrderItems().get(0).getId(), item.getId(), 3)
        ));

        assertThatThrownBy(() -> orderService.modifyOrder(order.getId(), modifyReq))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("주문 수정 실패 - 새 상품(newProduct)이 존재하지 않음")
    void modifyOrder_Fail_NewProductNotFound() {
        // 해당 코드 검증
        //Items newProduct = itemRepository.findById(reqItem.getItemId())
        //        .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

        Items item = new Items(null, "운동화", 50000, 10);
        itemRepository.save(item);

        Orders order = orderService.createOrder(
                new OrderCreateRequest(1L, List.of(new OrderItemRequest(item.getId(), 2)))
        );

        // 99999L는 없는 itemId
        OrderModifyRequest modifyReq = new OrderModifyRequest();
        modifyReq.setItems(List.of(
                new OrderModifyRequest.OrderModifyItem(order.getOrderItems().get(0).getId(), 99999L, 3)
        ));

        assertThatThrownBy(() -> orderService.modifyOrder(order.getId(), modifyReq))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다");
    }


    @Test
    @DisplayName("주문 수정 실패 - 수량이 0 이하")
    void modifyOrder_Fail_InvalidQuantity() {

        // 해당 코드 검증
        //if (newQty <= 0) {
        //    throw new AppException(ErrorCode.INVALID_QUANTITY);
        //}

        Items item = new Items(null, "운동화", 50000, 10);
        itemRepository.save(item);

        Orders order = orderService.createOrder(
                new OrderCreateRequest(1L, List.of(new OrderItemRequest(item.getId(), 2)))
        );

        OrderModifyRequest modifyReq = new OrderModifyRequest();
        modifyReq.setItems(List.of(
                new OrderModifyRequest.OrderModifyItem(order.getOrderItems().get(0).getId(),
                        item.getId(), 0)
        ));

        assertThatThrownBy(() -> orderService.modifyOrder(order.getId(), modifyReq))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("주문 수량은 1 이상이어야 합니다.");
    }

    @Test
    @DisplayName("주문 수정 실패 - 수량 증가 시 재고 부족")
    void modifyOrder_Fail_OutOfStock_QuantityIncrease() {

        //해당 코드 검증
        //if (newQty > oldQty) {
        //    int diff = newQty - oldQty;
        //    if (newProduct.getStockQuantity() < diff) {
        //        throw new AppException(ErrorCode.OUT_OF_STOCK);
        //    }
        //}

        Items item = new Items(null, "운동화", 50000, 3);
        itemRepository.save(item);

        Orders order = orderService.createOrder(
                new OrderCreateRequest(1L, List.of(new OrderItemRequest(item.getId(), 2)))
        );

        // 재고는 1개 남은 상태
        OrderModifyRequest modifyReq = new OrderModifyRequest();
        modifyReq.setItems(List.of(
                new OrderModifyRequest.OrderModifyItem(order.getOrderItems().get(0).getId(),
                        item.getId(), 10)
        ));

        assertThatThrownBy(() -> orderService.modifyOrder(order.getId(), modifyReq))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("재고가 부족");
    }

    @Test
    @DisplayName("주문 수정 실패 - 상품 교체 시 새 상품 재고 부족")
    void modifyOrder_Fail_OutOfStock_ChangeProduct() {
        //해당 코드 검증
        //if (!old.getItemId().equals(reqItem.getItemId())) {
        //    // 이전 상품 재고 복구
        //    // 새 상품 재고 감소
        //    if (newProduct.getStockQuantity() < newQty) {
        //        throw new AppException(ErrorCode.OUT_OF_STOCK);
        //    }
        //}

        Items oldItem = new Items(null, "운동화", 50000, 10);
        Items newItem = new Items(null, "모자", 15000, 0); // 재고 0
        itemRepository.save(oldItem);
        itemRepository.save(newItem);

        Orders order = orderService.createOrder(
                new OrderCreateRequest(1L, List.of(new OrderItemRequest(oldItem.getId(), 2)))
        );

        // oldItem(2개) → newItem(10개) 로 변경
        OrderModifyRequest modifyReq = new OrderModifyRequest();
        modifyReq.setItems(List.of(
                new OrderModifyRequest.OrderModifyItem(order.getOrderItems().get(0).getId(),
                        newItem.getId(), 10)
        ));

        assertThatThrownBy(() -> orderService.modifyOrder(order.getId(), modifyReq))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("재고가 부족");
    }


    //주문 취소 실패 검증

    @Test
    @DisplayName("주문 취소 실패 - 존재하지 않는 주문")
    void cancelOrder_Fail_OrderNotFound() {
        assertThatThrownBy(() -> orderService.cancelOrder(9999L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("주문을 찾을 수 없습니다.");
    }


    @Test
    @DisplayName("주문 취소 실패 - 이미 취소된 주문")
    void cancelOrder_Fail_AlreadyCancelled() {

        // given
        Items item = new Items(null, "운동화", 50000, 10);
        itemRepository.save(item);

        OrderCreateRequest req = new OrderCreateRequest();
        req.setUserId(1L);
        req.setOrderItems(List.of(new OrderItemRequest(item.getId(), 2)));

        Orders order = orderService.createOrder(req);

        // 1차 취소 성공
        orderService.cancelOrder(order.getId());

        // 2차 취소 시 실패해야 함
        assertThatThrownBy(() -> orderService.cancelOrder(order.getId()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("이미 취소된 주문입니다.");
    }

    @Test
    @DisplayName("주문 취소 실패 - 주문 상세(orderItems)가 없음")
    void cancelOrder_Fail_OrderItemsNotFound() {

        // given (강제로 orderItems 없이 주문 생성)
        Orders order = new Orders();
        order.setUserId(1L);
        order.setStatus("NEW");
        order.setTotalPrice(0);

        //빈 주문 아이템 리스트를 만듬
        order.setOrderItems(new ArrayList<>());

        // orderItems 없이 DB에 저장
        orderRepository.save(order);

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(order.getId()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("주문 상품을 찾을 수 없습니다.")
                .withFailMessage("orderItems가 비어있는데 예외가 발생하지 않았습니다.");
    }


    @Test
    @DisplayName("주문 취소 실패 - 주문 상품 없음(itemId 잘못됨)")
    void cancelOrder_Fail_ItemNotFound() {

        // given
        Items realItem = new Items(null, "운동화", 50000, 10);
        itemRepository.save(realItem);

        // 주문 생성 (정상 itemId로 생성)
        OrderCreateRequest req = new OrderCreateRequest();
        req.setUserId(1L);
        req.setOrderItems(List.of(
                new OrderItemRequest(realItem.getId(), 2)
        ));

        Orders order = orderService.createOrder(req);

        // 주문 상세(orderItems)의 itemId를 "존재하지 않는 ID"로 덮어쓰기
        Long invalidItemId = 99999L;
        order.getOrderItems().get(0).setItemId(invalidItemId);

        // orderItems 리스트를 DB에 반영 (수정 저장)
        orderRepository.updateOrderItems(order.getId(), order.getOrderItems());

        // when & then
        assertThatThrownBy(() -> orderService.cancelOrder(order.getId()))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("상품을 찾을 수 없습니다")
                .withFailMessage("유효하지 않은 itemId인데 예외가 발생하지 않았습니다.");
    }

    @Test
    @DisplayName("주문 취소 후 totalPrice 변화 검증")
    void cancelOrder_Check_TotalPriceChanged() {

        // given
        Items item1 = new Items(null, "운동화", 50000, 10);
        Items item2 = new Items(null, "모자", 15000, 20);

        itemRepository.save(item1);
        itemRepository.save(item2);

        // 주문 생성 (운동화 2개, 모자 1개)
        OrderCreateRequest req = new OrderCreateRequest();
        req.setUserId(1L);
        req.setOrderItems(List.of(
                new OrderItemRequest(item1.getId(), 2),  // 100,000
                new OrderItemRequest(item2.getId(), 1)   // 15,000
        ));

        Orders order = orderService.createOrder(req);
        int beforeTotalPrice = order.getTotalPrice();  // 예상 115,000

        // when
        OrderCancelResponse cancelled = orderService.cancelOrder(order.getId());

        // then
        Orders cancelledOrder = orderRepository.findById(order.getId());

        // totalPrice는 취소되어도 원래 가격 유지됨
        assertThat(cancelledOrder.getTotalPrice())
                .isEqualTo(beforeTotalPrice)
                .withFailMessage("주문 취소 후 totalPrice가 변경되면 안됩니다.");

        // status는 CANCELLED로 변경되어야 함
        assertThat(cancelledOrder.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("주문 단건 조회 실패 - 존재하지 않는 주문")
    void findOrder_Fail_OrderNotFound() {

        assertThatThrownBy(() -> orderService.findOrder(99999L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("주문을 찾을 수 없습니다");
    }



}
