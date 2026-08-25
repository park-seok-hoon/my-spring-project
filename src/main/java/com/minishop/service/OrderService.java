package com.minishop.service;

import com.minishop.domain.item.Item;
import com.minishop.domain.order.Order;
import com.minishop.domain.order.OrderItem;
import com.minishop.domain.order.OrderStatus;
import com.minishop.domain.user.User;
import com.minishop.dto.order.OrderCreateRequest;
import com.minishop.dto.order.OrderItemRequest;
import com.minishop.dto.order.OrderModifyRequest;
import com.minishop.dto.order.OrderUpdateRequest;
import com.minishop.exception.AppException;
import com.minishop.exception.ErrorCode;
import com.minishop.repository.ItemRepository;
import com.minishop.repository.OrderItemsRepository;
import com.minishop.repository.OrderRepository;
import com.minishop.repository.UserRepository;
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
    private final UserRepository userRepository;

    // 주문하기
    @Transactional
    public Order createOrder(OrderCreateRequest request) {

        if (request.getOrderItems() == null || request.getOrderItems().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.NEW);

        int totalPrice = 0;

        for (OrderItemRequest req : request.getOrderItems()) {

            Item item = itemRepository.findById(req.getItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

            if (item.getPrice() < 0) {
                throw new AppException(ErrorCode.INVALID_PRICE);
            }
            if (req.getQuantity() <= 0) {
                throw new AppException(ErrorCode.INVALID_QUANTITY);
            }
            if (item.getStockQuantity() < req.getQuantity()) {
                throw new AppException(ErrorCode.OUT_OF_STOCK);
            }

            item.setStockQuantity(item.getStockQuantity() - req.getQuantity());

            long linePrice = (long) item.getPrice() * req.getQuantity();
            if (linePrice > Integer.MAX_VALUE) {
                throw new AppException(ErrorCode.PRICE_OVERFLOW);
            }
            totalPrice += (int) linePrice;

            OrderItem orderItem = new OrderItem();
            orderItem.setItem(item);
            orderItem.setQuantity(req.getQuantity());
            orderItem.setOrder(order); // 연관관계 주인 쪽에 반드시 세팅

            order.getOrderItems().add(orderItem); // cascade=ALL 이라 별도 save 불필요
        }

        order.setTotalPrice(totalPrice);

        orderRepository.save(order);
        return order;
    }

    // 주문 단건 조회
    public Order findOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
    }

    // 전체 주문 조회
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    // 주문 상태 변경
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderUpdateRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        OrderStatus currentStatus = order.getOrderStatus();
        OrderStatus newStatus = parseStatus(request.getStatus());

        validateStatusTransition(currentStatus, newStatus);

        if (newStatus == OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setOrderStatus(newStatus); // 더티 체킹으로 자동 UPDATE

        return order;
    }

    private OrderStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }
        try {
            return OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_STATUS);
        }
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        if (current == OrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.ALREADY_CANCELLED);
        }
        if (current == OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_COMPLETED);
        }
        if (current == OrderStatus.SHIPPED && next == OrderStatus.NEW) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
        if (current == OrderStatus.SHIPPED && next == OrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.INVALID_STATUS_TRANSITION);
        }
    }

    // 재고 복구
    private void restoreStock(Order order) {
        for (OrderItem oi : order.getOrderItems()) {
            Item item = oi.getItem();
            item.setStockQuantity(item.getStockQuantity() + oi.getQuantity());
        }
    }

    // 주문 취소
    @Transactional
    public OrderCancelResponse cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.ALREADY_CANCELLED);
        }
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        List<OrderCancelResponse.ItemCancelInfo> restoredItems = new ArrayList<>();

        for (OrderItem orderItem : order.getOrderItems()) {
            Item item = orderItem.getItem();
            int qty = orderItem.getQuantity();

            item.setStockQuantity(item.getStockQuantity() + qty);

            restoredItems.add(
                    new OrderCancelResponse.ItemCancelInfo(
                            item.getId(),
                            item.getName(),
                            qty,
                            item.getStockQuantity()
                    )
            );
        }

        order.setOrderStatus(OrderStatus.CANCELLED);

        return new OrderCancelResponse(orderId, restoredItems);
    }

    // 주문 수정
    @Transactional
    public Order modifyOrder(Long orderId, OrderModifyRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderStatus() == OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.CANNOT_MODIFY_COMPLETED);
        }

        List<OrderItem> oldItems = order.getOrderItems();
        if (oldItems == null || oldItems.isEmpty()) {
            throw new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND);
        }

        Map<Long, OrderItem> oldMap = oldItems.stream()
                .collect(Collectors.toMap(OrderItem::getId, oi -> oi));

        int newTotalPrice = 0;

        for (OrderModifyRequest.OrderModifyItem reqItem : request.getItems()) {

            OrderItem old = oldMap.get(reqItem.getOrderItemId());
            if (old == null) {
                throw new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND);
            }

            Item oldProduct = old.getItem();
            Item newProduct = itemRepository.findById(reqItem.getItemId())
                    .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));

            int oldQty = old.getQuantity();
            int newQty = reqItem.getQuantity();

            if (newQty <= 0) {
                throw new AppException(ErrorCode.INVALID_QUANTITY);
            }

            if (oldProduct.getId().equals(newProduct.getId())) {
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
            } else {
                oldProduct.setStockQuantity(oldProduct.getStockQuantity() + oldQty);

                if (newProduct.getStockQuantity() < newQty) {
                    throw new AppException(ErrorCode.OUT_OF_STOCK);
                }
                newProduct.setStockQuantity(newProduct.getStockQuantity() - newQty);
            }

            old.setItem(newProduct);
            old.setQuantity(newQty);

            newTotalPrice += newProduct.getPrice() * newQty;
        }

        order.setTotalPrice(newTotalPrice);

        return order;
    }
}