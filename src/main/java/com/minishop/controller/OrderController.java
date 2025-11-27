// controller/OrderController.java
package com.minishop.controller;

import com.minishop.domain.Orders;
import com.minishop.dto.order.OrderCreateRequest;
import com.minishop.dto.order.OrderModifyRequest;
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
    public ResponseEntity<ApiResponse<Orders>> createOrder(@RequestBody OrderCreateRequest request) {
        Orders saved = orderService.createOrder(request);
        URI location = URI.create("/orders/" + saved.getId());
        return ResponseEntity.created(location)
                .body(ApiResponse.success("주문 생성 성공", saved));
    }

    // 단일 주문 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Orders>> getOrder(@PathVariable Long id) {
        log.info("컨트롤러 부분 GET /orders/{} 요청", id);
        Orders order = orderService.findOrder(id);
        return ResponseEntity.ok(ApiResponse.success("주문 조회 성공", order));
    }

    // 전체 주문 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<Orders>>> getAllOrders() {
        List<Orders> orders = orderService.findAllOrders();
        return ResponseEntity.ok(ApiResponse.success("주문 전체 조회 성공", orders));
    }

    // 주문 상태 변경
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Orders>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderUpdateRequest request
    ) {
        Orders updated = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("주문 상태 변경 성공", updated));
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
    public ResponseEntity<ApiResponse<Orders>> modifyOrder(
            @PathVariable Long id,
            @RequestBody OrderModifyRequest request) {

        Orders updated = orderService.modifyOrder(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("주문 수정 성공", updated)
        );

    }
}
