# 🛒 Order Module — 주문 시스템 전체 구현 (MiniShop Project)

`feature/order-module` 브랜치는 MiniShop 프로젝트에서 **주문(Order)** 기능 전체를 구현·개선한 핵심 브랜치입니다.  
일반적인 CRUD 수준을 넘어서 **재고 관리 · 주문 상태 흐름 · 트랜잭션 처리 · JOIN 기반 조회 · DTO 계층 분리 · 테스트 안정화**까지 실제 전자상거래 도메인 구조를 반영했습니다.

<br>

---

# 📌 구현 목표

- 실무형 주문 시스템을 정확히 모델링
- 주문/수정/취소 시 재고 감소·증가가 자동 처리
- 주문 상태(State Machine) 기반 제약 적용
- @Transactional 기반 데이터 정합성 보장
- DTO로 요청/응답 계층 분리 → Entity 노출 방지
- JOIN + resultMap으로 Orders → OrderItems → Items 구조 매핑
- 테스트 환경에서 rollback/deleteAll로 데이터 간섭 제거

<br>

---

# 🚀 제공 기능 전체 정리

| 기능 | 설명 |
|------|------|
| 주문 생성 | 재고 차감 + 총 금액 계산 + order_items 저장 |
| 주문 단건 조회 | Orders + OrderItems + Items JOIN 조회 |
| 주문 전체 조회 | 날짜 최근순 정렬 |
| 주문 상태 변경 | NEW → SHIPPED → COMPLETED 등 상태 전이 |
| 주문 취소 | 재고 복구 + 상세 취소 정보 반환 |
| 주문 수정 | 상품 변경 / 수량 변경 / 재고 증감 |
| 재고 검증 | 음수/부족/오류 예외 처리 |
| 상태 기반 제약 | CANCELLED 재취소 금지, COMPLETED 수정 금지 |
| 전체 테스트 | 성공/실패 테스트 + rollback |

<br>

---

# 🧩 도메인 구조 (Orders / OrderItems / Items)

```
Orders
 ├─ id
 ├─ userId
 ├─ orderDate
 ├─ totalPrice
 ├─ status (NEW, CANCELLED, SHIPPED, COMPLETED)
 └─ List<OrderItems>

OrderItems
 ├─ id
 ├─ orderId
 ├─ itemId
 ├─ quantity
 └─ Items item

Items
 ├─ id
 ├─ name
 ├─ price
 └─ stockQuantity
```

<br>

---

# 📘 DTO 구조 (Request / Response)

## 1) OrderCreateRequest — 주문 생성 요청 DTO

```java
@Data
@RequiredArgsConstructor
public class OrderCreateRequest {
    private Long userId;
    private List<OrderItemRequest> orderItems = new ArrayList<>();

    public OrderCreateRequest(long userId, List<OrderItemRequest> orderItemRequests) {
        this.userId = userId;
        this.orderItems = orderItemRequests;
    }
}
```

### ✔ 역할  
- 주문 생성 시 사용자 ID + 주문 상품 목록 전달
- null 방지를 위해 기본 리스트 초기화

---

## 2) OrderItemRequest — 주문 생성 시 개별 상품 DTO

```java
@Data
@AllArgsConstructor
public class OrderItemRequest {
    private Long itemId;
    private int quantity;
}
```

### ✔ 역할  
- "상품 ID + 수량" 단위로 주문 구성

---

## 3) OrderModifyRequest — 주문 상품/수량 수정 DTO

```java
@Data
public class OrderModifyRequest {
    private List<OrderModifyItem> items;

    @Data
    @AllArgsConstructor
    public static class OrderModifyItem {
        private Long orderItemId;
        private Long itemId;
        private int quantity;
    }
}
```

### ✔ 역할  
- order_items 행(row) 수정용 DTO
- 상품 변경 / 수량 변경 모두 처리

---

## 4) OrderUpdateRequest — 주문 상태 변경 DTO

```java
@Data
public class OrderUpdateRequest {
    private String status;
}
```

### ✔ 역할  
- 주문 상태 값만 변경 (NEW, CANCELLED, SHIPPED, COMPLETED)

---

## 5) OrderCancelResponse — 주문 취소 상세 응답 DTO

```java
public class OrderCancelResponse {
    private Long orderId;
    private List<ItemCancelInfo> items;

    public static class ItemCancelInfo {
        private Long itemId;
        private String itemName;
        private int canceledQuantity;
        private int restoredStock;
    }
}
```

### ✔ 역할  
- 취소된 각 상품의  
  - 취소 수량  
  - 복구 후 재고  
  상세 정보를 포함하는 응답

<br>

---

# 🔥 Controller — 주문 API

```java
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<Orders>> createOrder(@RequestBody OrderCreateRequest request) {
        Orders saved = orderService.createOrder(request);
        URI location = URI.create("/orders/" + saved.getId());
        return ResponseEntity.created(location)
                .body(ApiResponse.success("주문 생성 성공", saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Orders>> getOrder(@PathVariable Long id) {
        Orders order = orderService.findOrder(id);
        return ResponseEntity.ok(ApiResponse.success("주문 조회 성공", order));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Orders>>> getAllOrders() {
        List<Orders> orders = orderService.findAllOrders();
        return ResponseEntity.ok(ApiResponse.success("주문 전체 조회 성공", orders));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Orders>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody OrderUpdateRequest request
    ) {
        Orders updated = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("주문 상태 변경 성공", updated));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderCancelResponse>> cancelOrder(@PathVariable Long id) {
        OrderCancelResponse result = orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("주문 취소 성공", result));
    }

    @PutMapping("/{id}/items")
    public ResponseEntity<ApiResponse<Orders>> modifyOrder(
            @PathVariable Long id,
            @RequestBody OrderModifyRequest request) {

        Orders updated = orderService.modifyOrder(id, request);
        return ResponseEntity.ok(ApiResponse.success("주문 수정 성공", updated));
    }
}
```

<br>

---

# 🔥 Service — 주문 로직 핵심 (@Transactional)

## ✔ 주문 생성(createOrder)
- 재고 검증 → 재고 차감 → 금액 계산
- order_items 생성
- 트랜잭션 기반으로 전체 처리 안정화

## ✔ 주문 조회(findOrder)
- JOIN 매핑된 Orders 전체 반환
- OrderItems 없으면 빈 리스트 처리

## ✔ 주문 상태 변경(updateOrderStatus)
- 상태 전이 검증(State Machine)
- CANCELLED → 재고 복구 자동 처리

## ✔ 주문 취소(cancelOrder)
- 이미 취소면 오류
- 재고 복구 + 복구 상세 DTO 응답

## ✔ 주문 수정(modifyOrder)
- 상품 변경 시  
  - 이전 상품 재고 복구  
  - 새 상품 재고 감소
- 수량 변경 시  
  - 증가 → 재고 차감  
  - 감소 → 재고 복구
- order_items 업데이트

트랜잭션으로 전체가 하나의 원자적 작업으로 수행됨.

<br>

---

# 🗂 MyBatis Mapper — resultMap 기반 JOIN 구조

<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.minishop.repository.mybatis.mapper.OrderMapper">

    <!-- Orders 전체 매핑 -->
    <resultMap id="orderResultMap" type="Orders">
        <id property="id" column="order_id"/>
        <result property="userId" column="user_id"/>
        <result property="orderDate" column="order_date"/>
        <result property="totalPrice" column="total_price"/>
        <result property="status" column="status"/>

        <!-- OrderItems 목록 -->
        <collection property="orderItems"
                    ofType="OrderItems"
                    resultMap="orderItemResultMap"/>
    </resultMap>

    <!-- OrderItems + Items -->
    <resultMap id="orderItemResultMap" type="OrderItems">
        <id property="id" column="oi_id"/>
        <result property="orderId" column="oi_order_id"/>
        <result property="itemId" column="oi_item_id"/>
        <result property="quantity" column="quantity"/>

        <association property="item" javaType="Items">
            <id property="id" column="i_item_id"/>
            <result property="name" column="item_name"/>
            <result property="price" column="item_price"/>
            <result property="stockQuantity" column="item_stock"/>
        </association>
    </resultMap>

    <!-- 주문 저장 -->
    <insert id="insertOrder"
            parameterType="Orders"
            useGeneratedKeys="true"
            keyProperty="id">
        INSERT INTO orders (user_id, order_date, total_price, status)
        VALUES (#{userId}, #{orderDate}, #{totalPrice}, #{status})
    </insert>

    <!-- 주문상품 저장 -->
    <insert id="insertOrderItem"
            parameterType="OrderItems"
            useGeneratedKeys="true"
            keyProperty="id">
        INSERT INTO order_items (order_id, item_id, quantity)
        VALUES (#{orderId}, #{itemId}, #{quantity})
    </insert>

    <!-- 주문 단건 조회 -->
    <select id="findById"
            parameterType="long"
            resultMap="orderResultMap">

        SELECT
        /* Orders */
        o.id             AS order_id,
        o.user_id        AS user_id,
        o.order_date     AS order_date,
        o.total_price    AS total_price,
        o.status         AS status,

        /* OrderItems */
        oi.id            AS oi_id,
        oi.order_id      AS oi_order_id,
        oi.item_id       AS oi_item_id,
        oi.quantity      AS quantity,

        /* Items */
        i.id             AS i_item_id,
        i.name           AS item_name,
        i.price          AS item_price,
        i.stock_quantity AS item_stock

        FROM orders o
        LEFT JOIN order_items oi ON o.id = oi.order_id
        LEFT JOIN items i ON oi.item_id = i.id
        WHERE o.id = #{id}
    </select>

    <!-- 전체 주문 조회 -->
    <select id="findAll" resultType="Orders">
        SELECT
        id AS id,
        user_id AS userId,
        order_date AS orderDate,
        total_price AS totalPrice,
        status AS status
        FROM orders
        ORDER BY order_date DESC
    </select>

    <!-- 주문 상태 변경 -->
    <update id="updateOrderStatus" parameterType="map">
        UPDATE orders
        SET status = #{status}
        WHERE id = #{orderId}
    </update>

    <!-- 주문 수정 -->
    <update id="updateOrder">
        UPDATE orders
        SET
            user_id = #{userId},
            status = #{status},
            total_price = #{totalPrice}
        WHERE id = #{id}
    </update>

    <!-- 총액 변경 -->
    <update id="updateTotalPrice" parameterType="map">
        UPDATE orders
        SET total_price = #{totalPrice}
        WHERE id = #{orderId}
    </update>

    <!-- 주문 아이템 아이디 변경 -->
    <update id="updateOrderItems">
        <foreach collection="orderItems" item="oi" separator=";">
            UPDATE order_items
            SET
                item_id = #{oi.itemId},
                quantity = #{oi.quantity}
            WHERE id = #{oi.id}
              AND order_id = #{id}
        </foreach>
    </update>

    <!-- 전체 삭제 (테스트용) -->
    <delete id="deleteAll">
        DELETE FROM order_items;
        DELETE FROM orders;
        DELETE FROM items;
        DELETE FROM users;
    </delete>

</mapper>

---

<br>

---

# 📈 이 브랜치에서 직접 해결한 문제들 (정리)

- 트랜잭션(@Transactional)로 주문/수정/취소 안정성 확보
- JOIN 매핑 실패 → resultMap으로 해결
- 재고 감소/복구 로직 직접 설계
- 상태(State) 기반 로직 도입 (실무 **State Machine** 패턴 체득)
- DTO 필요성 제대로 이해 (Entity 노출 금지)
- 테스트 시 DB 데이터 누적 → rollback으로 해결
- deleteAll + BeforeEach로 테스트 독립성 확보
- Optional 사용 패턴 이해(Null 안전성 확보)

<br><br>
