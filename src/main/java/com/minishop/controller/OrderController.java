package com.minishop.controller;

import com.minishop.domain.order.Order;
import com.minishop.dto.order.OrderCreateRequest;
import com.minishop.dto.order.OrderModifyRequest;
import com.minishop.dto.order.OrderResponse;
import com.minishop.dto.order.OrderUpdateRequest;
import com.minishop.response.ApiResponse;
import com.minishop.response.OrderCancelResponse;
import com.minishop.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 주문하기
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@RequestBody OrderCreateRequest request) {
        Order saved = orderService.createOrder(request);
        URI location = URI.create("/orders/" + saved.getId());
        return ResponseEntity.created(location)
                .body(ApiResponse.success("주문 생성 성공", OrderResponse.from(saved)));
    }

    // 단일 주문 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long id) {
        log.info("컨트롤러 부분 GET /orders/{} 요청", id);
        Order order = orderService.findOrder(id);
        return ResponseEntity.ok(ApiResponse.success("주문 조회 성공", OrderResponse.from(order)));
    }

    // 전체 주문 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        List<Order> orders = orderService.findAllOrders();
        List<OrderResponse> responses = orders.stream()
                .map(OrderResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success("주문 전체 조회 성공", responses));
    }

    // 주문 상태 변경
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderUpdateRequest request
    ) {
        Order updated = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("주문 상태 변경 성공", OrderResponse.from(updated)));
    }

    // 주문 취소 + 재고 복구
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderCancelResponse>> cancelOrder(@PathVariable Long id) {
        OrderCancelResponse result = orderService.cancelOrder(id);
        return ResponseEntity.ok(
                ApiResponse.success("주문 취소 성공", result)
        );
    }

    // 주문 수정 (상품/수량 변경)
    @PutMapping("/{id}/items")
    public ResponseEntity<ApiResponse<OrderResponse>> modifyOrder(
            @PathVariable Long id,
            @RequestBody OrderModifyRequest request) {

        Order updated = orderService.modifyOrder(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("주문 수정 성공", OrderResponse.from(updated))
        );
    }
}