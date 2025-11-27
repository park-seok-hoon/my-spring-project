// service/OrderService.java
package com.minishop.service;

import com.minishop.domain.Items;
import com.minishop.domain.Orders;
import com.minishop.domain.OrderItems;
import com.minishop.dto.order.OrderCreateRequest;
import com.minishop.dto.order.OrderItemRequest;
import com.minishop.dto.order.OrderModifyRequest;
import com.minishop.dto.order.OrderUpdateRequest;
import com.minishop.exception.AppException;
import com.minishop.exception.ErrorCode;
import com.minishop.repository.ItemRepository;
import com.minishop.repository.OrderItemsRepository;
import com.minishop.repository.OrderRepository;
import com.minishop.response.OrderCancelResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemsRepository orderItemsRepository;

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

    //주문 상태 변경
    @Transactional
    public Orders updateOrderStatus(Long orderId, OrderUpdateRequest request) {

        // 1) 주문 조회
        Orders order = orderRepository.findById(orderId);
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        String currentStatus = order.getStatus();
        String newStatus = request.getStatus();

        // 2) 상태 값 검증
        if (newStatus == null || newStatus.isBlank()) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }

        if (!isValidStatus(newStatus)) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }

        // 3) 상태 전이 규칙 검증
        validateStatusTransition(currentStatus, newStatus);

        // 4) CANCELLED로 전환될 때 재고 복구
        if (newStatus.equals("CANCELLED")) {
            restoreStock(order);
        }

        // 5) 상태 변경
        order.setStatus(newStatus);
        orderRepository.updateStatus(orderId, newStatus);

        return order;
    }


    // 상태 전환 규칙
    private void validateStatusTransition(String current, String next) {

        if (current.equals("CANCELLED")) {
            throw new AppException(ErrorCode.ALREADY_CANCELLED);
        }

        if (current.equals("COMPLETED")) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_COMPLETED);
        }

        if (current.equals("SHIPPED") && next.equals("NEW")) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
        }

        if (current.equals("SHIPPED") && next.equals("CANCELLED")) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }


    // 재고 복구
    private void restoreStock(Orders order) {
        for (OrderItems oi : order.getOrderItems()) {
            Items item = itemRepository.findById(oi.getItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

            item.setStockQuantity(item.getStockQuantity() + oi.getQuantity());
            itemRepository.update(item);
        }
    }



    private boolean isValidStatus(String status) {
        // NEW : 주문 생성, CANCELLED : 주문 취소 , SHIPPED : 배송중 , COMPLETED : 배송완료
        boolean b = status.equals("NEW") ||
                status.equals("CANCELLED") ||
                status.equals("SHIPPED") ||
                status.equals("COMPLETED");

        return b;
    }


    //주문 취소
    @Transactional
    public OrderCancelResponse cancelOrder(Long orderId) {

        // 1) 주문 조회
        Orders order = orderRepository.findById(orderId);

        //주문이 존재하지 않는 경우
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 2) 이미 취소된 주문인지 검사
        if ("CANCELLED".equals(order.getStatus())) {
            throw new AppException(ErrorCode.ALREADY_CANCELLED);
        }

        // 3) 주문 상품 조회가 null이거나 비어 있으면 오류
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        // 4) 재고 복구
        List<OrderCancelResponse.ItemCancelInfo> restoredItems = new ArrayList<>();

        for (OrderItems orderItem : order.getOrderItems()) {

            Items item = itemRepository.findById(orderItem.getItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

            //취소 전 주문 수량
            int qty = orderItem.getQuantity();

            // 재고 복구
            item.setStockQuantity(item.getStockQuantity() + qty);
            itemRepository.update(item);

            // 응답용 DTO 정보 저장
            restoredItems.add(
                    new OrderCancelResponse.ItemCancelInfo(
                            item.getId(),
                            item.getName(),
                            qty,          // 취소 전 주문 수량
                            item.getStockQuantity() // 복구 후 재고
                    )
            );
        }

        // 5) 상태 변경
        order.setStatus("CANCELLED");
        orderRepository.updateStatus(orderId, "CANCELLED");

        // 6) DTO 반환
        return new OrderCancelResponse(orderId, restoredItems);
    }



    //주문 수정
    @Transactional
    public Orders modifyOrder(Long orderId, OrderModifyRequest request) {

        // 0) 요청 검증
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // 1) 주문 조회
        Orders order = orderRepository.findById(orderId);
        if (order == null) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        // 주문 상태가 완료된 경우 수정 불가
        if ("COMPLETED".equals(order.getStatus())) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_COMPLETED);
        }

        // 2) 기존 주문상품 리스트
        List<OrderItems> oldItems = order.getOrderItems();

        if (oldItems == null || oldItems.isEmpty()) {
            throw new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        // map으로 접근 속도 증가
        Map<Long, OrderItems> oldMap = oldItems.stream()
                .collect(Collectors.toMap(OrderItems::getId, oi -> oi));

        int newTotalPrice = 0;

        // 3) 요청된 items 하나씩 처리
        for (OrderModifyRequest.OrderModifyItem reqItem : request.getItems()) {

            OrderItems old = oldMap.get(reqItem.getOrderItemId());
            if (old == null) {
                throw new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND);
            }

            //구 상품 검증
            Items oldProduct = itemRepository.findById(old.getItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));


            //새 상품 검증
            Items newProduct = itemRepository.findById(reqItem.getItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

            int oldQty = old.getQuantity();
            int newQty = reqItem.getQuantity();

            // 수량 검증
            if (newQty <= 0) {
                throw new AppException(ErrorCode.INVALID_QUANTITY);
            }

            // 3-1) 수량 변경만 있는 경우
            if (old.getItemId().equals(reqItem.getItemId())) {

                //
                if (newQty > oldQty) {
                    int diff = newQty - oldQty;
                    if (newProduct.getStockQuantity() < diff) {
                        throw new AppException(ErrorCode.OUT_OF_STOCK);
                    }
                    newProduct.setStockQuantity(newProduct.getStockQuantity() - diff);
                } else if (newQty < oldQty) {
                    int diff = oldQty - newQty;
                    newProduct.setStockQuantity(newProduct.getStockQuantity() + diff);
                }

                itemRepository.update(newProduct);

            } else {
                // 3-2) 상품 자체가 바뀐 경우
                // 이전 상품 재고 복구
                oldProduct.setStockQuantity(oldProduct.getStockQuantity() + oldQty);
                itemRepository.update(oldProduct);

                // 새 상품 재고 감소
                if (newProduct.getStockQuantity() < newQty) {
                    throw new AppException(ErrorCode.OUT_OF_STOCK);
                }
                newProduct.setStockQuantity(newProduct.getStockQuantity() - newQty);
                itemRepository.update(newProduct);
            }

            // 3-3) order_items 업데이트
            old.setItemId(reqItem.getItemId());
            old.setQuantity(newQty);
            orderItemsRepository.update(old);

            // 3-4) 금액 누적
            newTotalPrice += newProduct.getPrice() * newQty;
        }

        // 4) 최종 금액 업데이트
        order.setTotalPrice(newTotalPrice);
        orderRepository.updateTotalPrice(orderId, newTotalPrice);

        return orderRepository.findById(orderId);
    }


}
