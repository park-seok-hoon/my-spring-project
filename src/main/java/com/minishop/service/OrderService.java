// service/OrderService.java
package com.minishop.service;

import com.minishop.domain.Items;
import com.minishop.domain.Orders;
import com.minishop.domain.OrderItems;
import com.minishop.dto.order.OrderCreateRequest;
import com.minishop.dto.order.OrderItemRequest;
import com.minishop.dto.order.OrderUpdateRequest;
import com.minishop.exception.AppException;
import com.minishop.exception.ErrorCode;
import com.minishop.repository.ItemRepository;
import com.minishop.repository.OrderItemsRepository;
import com.minishop.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
//    private final OrderItemsRepository orderItemsRepository;

    //주문하기
    @Transactional
    public Orders createOrder(OrderCreateRequest request) {

        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        int totalPrice = 0;
        List<OrderItems> items = new ArrayList<>();

        for (OrderItemRequest req : request.getOrderItems()) {

            //상품 조회
            Items item = itemRepository.findById(req.getItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

            //가격 검증
            if (item.getPrice() < 0) {
                throw new AppException(ErrorCode.INVALID_PRICE);
            }

            //수량 검증
            if (req.getQuantity() <= 0) {
                throw new AppException(ErrorCode.INVALID_QUANTITY);
            }

            //재고 검증
            if (item.getStockQuantity() < req.getQuantity()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            // 재고 감소
            item.setStockQuantity(item.getStockQuantity() - req.getQuantity());

            //  DB 반영
            itemRepository.update(item);

            //각 상품 금액 계산
            long linePrice = (long) item.getPrice() * req.getQuantity();
            if (linePrice > Integer.MAX_VALUE) {
                throw new AppException(ErrorCode.PRICE_OVERFLOW);
            }

            totalPrice += (int) linePrice;

            //주문상품 준비
            OrderItems orderItem = new OrderItems();
            orderItem.setItemId(req.getItemId());
            orderItem.setQuantity(req.getQuantity());

            items.add(orderItem);
        }

        Orders order = new Orders();
        order.setUserId(request.getUserId());
        order.setOrderItems(items);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalPrice(totalPrice);
        order.setStatus("NEW");

        orderRepository.save(order);
        return order;
    }

    // 주문 단건 조회
    public Orders findOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId);

        //주문이 조회 되지 않는 경우
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 주문상품이 null이면 빈 리스트로 처리 (예외 아님)
        if (order.getOrderItems() == null) {
            order.setOrderItems(new ArrayList<>());
        }

        return order;
    }


    // 전체 주문 조회
    public List<Orders> findAllOrders() {
        List<Orders> orders = orderRepository.findAll();

        System.out.println(">>> orders 결과 = " + orders);

        if (orders == null || orders.isEmpty()) {
            // NPE(NullPointerException)를 막기 위해
            return Collections.emptyList();
        }

        return orders;
    }




    // 주문 수정
    @Transactional
    public Orders updateOrder(Long orderId, OrderUpdateRequest request) {

        Orders order = orderRepository.findById(orderId);

        log.info("service 에서 order 정보 {}",order);

//        if (order == null) {
//            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
//        }
//
//        // 업데이트 가능한 필드 적용
//        if (request.getUserId() != null) {
//            order.setUserId(request.getUserId());
//        }
//
//        if (request.getStatus() != null) {
//            order.setStatus(request.getStatus());
//        }

        // DB 반영
        orderRepository.update(order);

        return order;
    }

//    @Transactional
//    public Orders cancelOrder(Long orderId) {
//
//        Orders order = orderRepository.findById(orderId);
//
//        //주문이 없는 경우
//        if (order == null) {
//            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
//        }
//
//        //이미 취소된 주문이면 예외 발생
//        if ("CANCELLED".equals(order.getStatus())) {
//            throw new AppException(ErrorCode.ALREADY_CANCELLED);
//        }
//
//        // 주문상품 재고 복구
//        for (OrderItems item : order.getOrderItems()) { //주문했던 아이템들
//
//            Items dbItem = itemRepository.findById(item.getItemId())
//                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
//
//            dbItem.setStockQuantity(dbItem.getStockQuantity() + item.getQuantity());
//            itemRepository.update(dbItem); // 재고 업데이트
//        }
//
//        // 상태 변경
//        order.setStatus("CANCELLED");
//        orderRepository.cancel(orderId);
//
//        return order;
//    }
}
