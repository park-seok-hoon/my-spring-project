# 🛍️ Mini Shop — Spring Boot 실무형 CRUD & 주문 시스템

Java 17 · Spring Boot · MyBatis 기반의 전자상거래 실무 프로젝트입니다.  
단순 CRUD 수준이 아닌, **실무에서 꼭 필요한 예외 처리 · 재고 관리 · 트랜잭션 · DTO 적용 · JOIN 매핑 · 테스트 코드**까지 전부 경험하고 해결한 내용을 담고 있습니다.

---

## 📂 프로젝트 개요

- **프로젝트 이름**: Mini Shop
- **인원**: 1인 개인 프로젝트 (기획 · 설계 · 개발 · 테스트 전부 직접 수행)
- **역할**: 백엔드 전담
- **기간**: 2025.10 ~ 2025.11 (개인 공부 & 포트폴리오용)
- **GitHub**: [Mini Shop Repository](https://github.com/park-seok-hoon/my-spring-project)

> 단순한 예제 코드가 아니라,  
> “실제로 서비스된다고 가정하고 어떤 구조로 짤지”를 고민하며 만든 프로젝트입니다.

---

## 📌 프로젝트 목표

#### 실무적인 부분을 최대한 반영하여 프로젝트를 만들려고 노력하였습니다.

- Controller는 최대한 얇게, Service에서 핵심 비즈니스 처리
- 예외 처리 흐름 일원화 (AppException · ErrorCode · GlobalExceptionHandler)
- 주문/취소/수정 시 재고·상태·트랜잭션을 모두 고려한 구조
- MyBatis XML + JOIN + resultMap을 직접 설계해 보는 것
- 테스트 코드로 기능이 실제로 보장되도록 만드는 것

---

# 🚀 주요 기능 요약

## 📦 Item (상품)
- 상품 등록 / 조회 / 전체 조회 / 수정 / 삭제  
- 중복 상품명 검증  
- 음수 가격, 음수 재고 예외 처리  

## 👤 User (회원)
- 회원 가입 / 조회 / 수정 / 삭제  
- 이메일 중복 검증  
- 비밀번호 유효성 검사  

## 🛒 Order (주문)
- 주문 생성  
- 주문 상품 유효성 검증  
- 재고 차감  
- 총 금액 계산  
- 주문 취소  
  - 재고 복구  
  - 이미 취소된 주문 재취소 금지  
- 주문 수정 (수량 변경 + 금액 재계산)  
- 주문 전체 조회 / 단일 조회  
- JOIN 기반으로 주문 + 주문상품 + 상품을 한 번에 가져오는 조회 구현  

## 🧪 Test
- Item / User / Order 테스트 전체 구축  
- 정상/실패 시나리오 30개 이상  
- 테스트 간 간섭 제거 (deleteAll + @BeforeEach)  
- @Transactional 적용으로 테스트 데이터 자동 rollback  

---

# 🔄 주문 처리 Flowchart (시각 버전)
```
               [주문 생성]
클라이언트 요청
      ↓
사용자 존재 검증
      ↓
각 상품 존재 + 재고 검증
      ↓
수량만큼 재고 차감
      ↓
총 금액 계산
      ↓
주문 + 주문상품 저장
      ↓
응답 반환

              [주문 취소]
상태 확인(NEW만 가능)
      ↓
주문상품 전체 조회
      ↓
각 상품 재고 복구
      ↓
상태 CANCELLED 변경

```
---
# 📡 API 예시 (실제 요청/응답 샘플)

아래는 MiniShop 프로젝트에서 가장 핵심 기능들의 API 예시입니다.
실제로 테스트 환경에서 얻은 JSON을 기반으로 구성해 **“실무 문서 느낌”**을 살렸습니다.

---

# 📦 Item API

## ➕ 상품 등록 (POST /items)

**Request**

```json
{
  "name": "운동화",
  "price": 50000,
  "stockQuantity": 10
}
```

**Response**

```json
{
  "timestamp": "2025-11-25T14:10:22.123",
  "path": "/items",
  "code": "SUCCESS",
  "message": "상품 등록 성공",
  "data": {
    "id": 1,
    "name": "운동화",
    "price": 50000,
    "stockQuantity": 10
  }
}
```

---

## 📖 상품 단건 조회 (GET /items/1)

```json
{
  "timestamp": "2025-11-25T14:11:30.321",
  "path": "/items/1",
  "code": "SUCCESS",
  "message": "상품 조회 성공",
  "data": {
    "id": 1,
    "name": "운동화",
    "price": 50000,
    "stockQuantity": 10
  }
}
```

---

# 👤 User API

## ✍ 회원 가입 (POST /users)

**Request**

```json
{
  "username": "홍길동",
  "email": "test@example.com",
  "password": "password123"
}
```

**Response**

```json
{
  "timestamp": "2025-11-25T15:02:44.100",
  "path": "/users",
  "code": "SUCCESS",
  "message": "회원 등록 성공",
  "data": {
    "id": 1,
    "username": "홍길동",
    "email": "test@example.com"
  }
}
```

---

## 📖 회원 조회 (GET /users/1)

```json
{
  "timestamp": "2025-11-25T15:05:22.983",
  "path": "/users/1",
  "code": "SUCCESS",
  "message": "회원 조회 성공",
  "data": {
    "id": 1,
    "username": "홍길동",
    "email": "test@example.com"
  }
}
```

---

# 🛒 Order API

## 🆕 주문 생성 (POST /orders)

**Request**

```json
{
  "userId": 1,
  "items": [
    { "itemId": 1, "quantity": 2 },
    { "itemId": 2, "quantity": 1 }
  ]
}
```

**Response**

```json
{
  "timestamp": "2025-11-25T17:07:11.1075504",
  "path": "/orders",
  "code": "SUCCESS",
  "message": "주문 생성 성공",
  "data": {
    "id": 1,
    "userId": 1,
    "orderDate": "2025-11-25T17:07:11.107550",
    "totalPrice": 115000,
    "status": "NEW",
    "orderItems": [
      {
        "id": 1,
        "orderId": 1,
        "itemId": 1,
        "quantity": 2,
        "item": {
          "id": 1,
          "name": "운동화",
          "price": 50000,
          "stockQuantity": 8
        }
      },
      {
        "id": 2,
        "orderId": 1,
        "itemId": 2,
        "quantity": 1,
        "item": {
          "id": 2,
          "name": "모자",
          "price": 15000,
          "stockQuantity": 4
        }
      }
    ]
  }
}
```

---

## 📖 주문 단건 조회 (GET /orders/1)

```json
{
  "timestamp": "2025-11-25T17:12:06.717725",
  "path": "/orders/1",
  "code": "SUCCESS",
  "message": "주문 조회 성공",
  "data": {
    "id": 1,
    "userId": 1,
    "orderDate": "2025-11-25T17:12:06.717725",
    "totalPrice": 115000,
    "status": "NEW",
    "orderItems": [
      {
        "id": 1,
        "orderId": 1,
        "itemId": 1,
        "quantity": 2,
        "item": {
          "id": 1,
          "name": "운동화",
          "price": 50000,
          "stockQuantity": 8
        }
      }
    ]
  }
}
```

---

## 🔄 주문 수정 (PATCH /orders/{id}/items)

**Request**

```json
{
  "items": [
    { "itemId": 1, "quantity": 3 }, 
    { "itemId": 2, "quantity": 2 }
  ]
}
```

**Response**

```json
{
  "timestamp": "2025-11-26T16:30:55.5880564",
  "path": "/orders/1/items",
  "code": "SUCCESS",
  "message": "주문 수정 성공",
  "data": {
    "id": 1,
    "userId": 1,
    "orderDate": "2025-11-25T17:12:06.717725",
    "totalPrice": 295000,
    "status": "NEW",
    "orderItems": [
      {
        "id": 1,
        "orderId": 1,
        "itemId": 1,
        "quantity": 3,
        "item": {
          "id": 1,
          "name": "운동화",
          "price": 50000,
          "stockQuantity": 9
        }
      },
      {
        "id": 2,
        "orderId": 1,
        "itemId": 2,
        "quantity": 2,
        "item": {
          "id": 2,
          "name": "모자",
          "price": 15000,
          "stockQuantity": 3
        }
      }
    ]
  }
}
```

---

## ❌ 주문 취소 (PATCH /orders/{id}/cancel)

```json
{
  "timestamp": "2025-11-24T18:06:24.6005964",
  "path": "/orders/1/cancel",
  "code": "SUCCESS",
  "message": "주문 취소 성공",
  "data": {
    "id": 1,
    "status": "CANCELLED",
    "restoredStockItems": [
      { "itemId": 1, "restored": 2 },
      { "itemId": 2, "restored": 1 }
    ]
  }
}
```

---
## 🧩 주문 처리 흐름 (텍스트 플로우)

```text
[주문 생성]
1. 클라이언트에서 주문 생성 요청
2. User 존재 여부 확인
3. 각 Item 존재 및 재고 수량 검증
4. 주문 수량만큼 재고 차감
5. 총 주문 금액 계산
6. Orders + OrderItems 저장 (@Transactional)
7. ApiResponse 형태로 결과 반환

[주문 수정]
1. 기존 주문 조회
2. 주문 상태 확인 (NEW 상태만 수정 가능)
3. 기존 수량 vs 변경 수량 비교
4. 차이만큼 재고 차감/복구
5. 총 금액 재계산
6. 수정된 주문 정보 응답

[주문 취소]
1. 주문 상태 확인 (이미 CANCELLED면 예외)
2. 주문에 속한 모든 OrderItems 조회
3. 각 Item에 재고 복구
4. 주문 상태를 CANCELLED로 변경
5. 취소 결과 응답
````
---
# 🗂️ ERD (Entity Relationship Diagram)
```
Users (1)
   └─< Orders (N)
          └─< OrderItems (N)
Items (1)
```
테이블 간 관계:

- Users 1 : N Orders
- Orders 1 : N OrderItems
- Items 1 : N OrderItems
이 구조를 기반으로 JOIN 조회 및 resultMap 설계가 이루어짐.

---

# 🏗️ 아키텍처 구조

```text
API Request
    ↓
Controller  — 요청/응답 (SRP 적용)
    ↓
Service     — 비즈니스 로직 / 예외 처리 담당
    ↓
Repository  — MyBatis Mapper 호출
    ↓
MyBatis XML — 명시적 SQL (JOIN, association 매핑)
    ↓
H2 / MySQL  — 실제 데이터 저장
```

---

# 🔗 MyBatis JOIN 매핑 구조
## ✔ Order → OrderItems → Items JOIN 구조

Orders.xml 내부 resultMap 예시(요약):
```
<resultMap id="OrderResultMap" type="Orders">
    <id property="id" column="order_id"/>
    <result property="userId" column="user_id"/>
    <result property="totalPrice" column="total_price"/>
    <result property="status" column="status"/>

    <collection property="orderItems" ofType="OrderItems" resultMap="OrderItemsResultMap"/>
</resultMap>

<resultMap id="OrderItemsResultMap" type="OrderItems">
    <id property="id" column="order_item_id"/>
    <result property="quantity" column="quantity"/>

    <association property="item" javaType="Items" resultMap="ItemsResultMap"/>
</resultMap>
```
## ✔ 핵심 포인트
- Orders → List<OrderItems> (1:N)
- OrderItems → Items (N:1)
- 하나의 JOIN 쿼리로 전체 구조 매핑
- MyBatis에서 ORM처럼 작동하도록 직접 매핑 설계

## 📁 주요 디렉토리 구조

```text
src
└─ main
   ├─ java
   │  └─ com.minishop
   │      ├─ config
   │      │   └─ MiniShopApplication    # Spring Boot Application 실행 클래스
   │      │
   │      ├─ controller                 # REST API 계층 (요청/응답)
   │      │   ├─ ItemController
   │      │   ├─ OrderController
   │      │   └─ UserController
   │      │
   │      ├─ domain                     # 엔티티 (DB 테이블과 1:1 매핑되는 도메인)
   │      │   ├─ Items
   │      │   ├─ OrderItems
   │      │   ├─ Orders
   │      │   └─ Users
   │      │
   │      ├─ dto                        # 요청(Request) / 응답(Response) DTO
   │      │   ├─ item
   │      │   │    ├─ ItemCreateRequest
   │      │   │    └─ ItemUpdateRequest
   │      │   ├─ user
   │      │   │    ├─ UserCreateRequest
   │      │   │    └─ UserUpdateRequest
   │      │   └─ order
   │      │        ├─ OrderCreateRequest
   │      │        ├─ OrderUpdateRequest
   │      │        ├─ OrderModifyRequest
   │      │        └─ OrderUpdateRequest
   │      │
   │      ├─ exception                  # 공통 예외 처리 계층
   │      │   ├─ AppException           # 커스텀 런타임 예외
   │      │   ├─ ErrorCode              # 에러 코드 Enum
   │      │   ├─ ErrorResult            # 예외 응답 DTO
   │      │   └─ GlobalExceptionHandler # 전역 예외 핸들러
   │      │
   │      ├─ repository                 # Repository 계층 (인터페이스)
   │      │   └─ mybatis
   │      │        ├─ ItemRepository
   │      │        ├─ OrderItemsRepository
   │      │        ├─ OrderRepository
   │      │        └─ UserRepository
   │      │
   │      ├─ response                   # 공통 API Response 구조
   │      │   ├─ ApiResponse
   │      │   └─ OrderCancelResponse
   │      │
   │      └─ service                    # 비즈니스 로직 계층
   │          ├─ ItemService
   │          ├─ OrderService
   │          └─ UserService
   │
   ├─ resources
   │  ├─ mapper                         # MyBatis XML 매퍼 (SQL 존재)
   │  │   ├─ ItemMapper.xml
   │  │   ├─ OrderItemsMapper.xml
   │  │   ├─ OrderMapper.xml
   │  │   └─ UserMapper.xml
   │  │
   │  ├─ static
   │  ├─ templates
   │  └─ application.properties         # DB 설정 / 포트 설정 등
   │
   └─ test
      └─ java
         └─ com.minishop
              └─ service                # 서비스 단위/통합 테스트
                  ├─ ItemServiceTest
                  ├─ OrderServiceTest
                  └─ UserServiceTest

```

→ 실제로 “Controller → Service → Repository → Mapper → XML → DB” 흐름이
패키지 구조에서도 그대로 드러나도록 구성했습니다.

---

# ⚙️ 기술 스택

| 종류         | 기술                        |
| ---------- | ------------------------- |
| Language   | Java 17                   |
| Framework  | Spring Boot 3.5.x         |
| DB         | H2, MySQL                 |
| ORM/Mapper | MyBatis (XML 기반)          |
| Build      | Gradle                    |
| Test       | JUnit5, AssertJ           |
| 기타         | Lombok, Validation, Slf4j |

---

## ▶ 실행 방법 (Run Guide)

```bash
# 1. 프로젝트 클론
git clone https://github.com/park-seok-hoon/my-spring-project.git
cd my-spring-project

# 2. (선택) 프로필 / DB 설정
# application.yml 에서 H2 또는 MySQL 설정 선택

# 3. 빌드 및 실행
./gradlew bootRun   # (Windows에서는 gradlew.bat bootRun)

# 4. 기본 주소
# http://localhost:8080
```

* H2를 사용할 경우: 콘솔에서 스키마 자동 생성 후 바로 테스트 가능
* MySQL 사용 시: 스키마 생성 및 계정 정보만 맞춰주면 동일하게 동작

---

# 🗄️ H2 & MySQL 접속 정보
✔ H2 콘솔 접속
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:~/minishop
- USER: sa
- PW: (빈 값)
application.properties에서 변경 가능

✔ MySQL 연결
```
spring.datasource.url=jdbc:mysql://localhost:3306/minishop
spring.datasource.username=root
spring.datasource.password=1234
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

👨‍💻 내가 맡은 역할 & 해결한 핵심 난관

- MyBatis JOIN 매핑 실패 문제 직접 해결
- 상태 기반 주문 도메인(State Machine) 설계
- 재고 조정 로직(차감/복구)의 일관성 유지
- @Transactional 기반 주문/취소 원자성 보장
- 테스트 독립성 100% 확보(deleteAll + rollback)
- 예외 처리 흐름(AppException → ErrorCode → Handler) 완전 통합
- SRP 원칙에 맞게 코드 리팩토링 컨트롤러는 요청 응답만

# 🌿 브랜치 구조

| 브랜치                                                                                                                       | 설명                         |
| ------------------------------------------------------------------------------------------------------------------------- | -------------------------- |
| [`controller-exception`](https://github.com/park-seok-hoon/my-spring-project/tree/controller-exception)                   | Controller 중심 예외 처리 초기 버전  |
| [`service-exception`](https://github.com/park-seok-hoon/my-spring-project/tree/service-exception)                         | 예외 처리 · 검증 책임을 Service로 이전 |
| [`refactor/unified-api-response`](https://github.com/park-seok-hoon/my-spring-project/tree/refactor/unified-api-response) | API 응답 구조 통일 (ApiResponse) |
| [`feature/order-module`](https://github.com/park-seok-hoon/my-spring-project/tree/feature/order-module)                   | 주문 전체 기능 구현 + 통합 테스트 구축    |

> 브랜치별로 “문제 → 해결” 흐름이 드러나도록 커밋과 README를 작성했습니다.

---

# 🔧 개선 히스토리 요약 (내가 직접 해결한 문제 중심)

| 번호     | 날짜         | 해결한 문제                                          |
| ------ | ---------- | ----------------------------------------------- |
| **1**  | 2025.11.12 | Controller에 분산된 예외 처리 → Service 중심 구조로 개선       |
| **2**  | 2025.11.13 | MyBatis UPDATE에서 SET 중복 오류 해결                   |
| **3**  | 2025.11.15 | API 응답 포맷 통일 (ApiResponse + ErrorCode)          |
| **4**  | 2025.11.17 | Optional로 null 안전 처리 구조 확립                      |
| **5**  | 2025.11.18 | @Transactional 적용으로 데이터 정합성 보장                  |
| **6**  | 2025.11.19 | JOIN 결과 매핑 실패 → resultMap + List 매핑 해결          |
| **7**  | 2025.11.20 | 주문 상태(State) 개념 도입 → 상태 기반 로직 구축                |
| **8**  | 2025.11.22 | JOIN 매핑 계속 실패 → resultMap 제대로 설계하여 해결           |
| **9**  | 2025.11.24 | DTO 필요성 이해 → Entity 직접 노출 제거                    |
| **10** | 2025.11.26 | 테스트 간 간섭 문제 → 테스트 DB 초기화로 해결                    |
| **11** | 2025.11.27 | 테스트 시 DB 데이터 누적 문제 → @Transactional rollback 해결 |

---

# 🔥 상세 개선 히스토리

## 1) 예외 처리 구조 개선 — Controller → Service

> **문제**
> 예외 처리가 Controller에 분산되어 있어서 중복 코드가 많고 유지보수가 어려웠음.
>
> **해결**
>
> * Service가 모든 검증/비즈니스 예외 관리
> * Controller는 요청/응답만 담당 (SRP)
> * `AppException`, `ErrorCode`, `GlobalExceptionHandler` 도입
>
> **배운 점**
> 실무에서는 Controller를 최대한 얇게 유지해야 한다는 것을 확실히 이해함.

---

## 2) MyBatis UPDATE SET 중복 오류

> **문제**
> MyBatis XML에서 SET이 여러 번 작성되어 SQL 문법 오류 발생.
>
> **해결**
>
> * SET은 한 번만 쓰고 필드를 콤마로 구분
>
> **배운 점**
> MyBatis는 SQL을 직접 작성하므로 문법 체크가 매우 중요하다는 것.

---

## 3) 응답 형식이 제각각 → ApiResponse + ErrorCode로 통일

> **문제**
> API 응답 형식이 모두 다르다 보니 유지보수와 문서화가 매우 불편했음.
>
> **해결**
>
> * 모든 성공/실패 응답을 ApiResponse<T> 형태로 통일
> * ErrorCode(Enum)로 에러 코드를 중앙에서 관리
> * GlobalExceptionHandler 하나로 전부 관리
>
> **배운 점**
> “응답 구조 통일”은 실무 품질을 크게 올리는 핵심이라는 걸 느낌.

---

## 4) Optional 이해 — null 대신 안전한 방식 학습

> **문제**
> Optional을 왜 쓰는지 몰라 계속 if(null)만 사용함.
>
> **해결**
>
> * “존재할 수도, 안 할 수도 있는 값”에 Optional 사용
> * `orElseThrow()` 등 null-safe 코드 작성
>
> **배운 점**
> Optional은 null 위험을 줄이고 의도를 더 명확하게 표현하는 타입.

---

## 5) 트랜잭션 개념은 있는데 실제로 어떻게 적용하는지 몰랐음 → 해결

> **문제**
> 주문 생성/수정/취소 중 실패하면 이미 반영된 DB를 어떻게 복구해야 하는지 몰랐음.
>
> **해결**
>
> * @Transactional을 적용해 전체 로직을 하나의 단위로 묶음
> * 실패 시 자동 rollback 처리
>
> **배운 점**
> 트랜잭션이 “실패하면 이전 상태로 되돌린다”는 원리를 실전에서 확실히 이해함.

---

## 6) JOIN 결과가 자바 객체에 매핑되지 않음 → 구조 자체를 이해해서 해결

> **문제**
> Order + OrderItems + Items를 JOIN 했는데 자바 객체에는 null이 계속 들어감.
>
> **원인**
> 도메인 구조(1:N, N:1)를 Java 객체로 어떻게 표현해야 하는지 몰랐음.
>
> **해결**
>
> * Order에 List<OrderItems> 추가
> * OrderItems에 Items item 추가
> * MyBatis resultMap + association + collection 정확하게 설정
>
> **배운 점**
> MyBatis JOIN 매핑은 자동이 아니라 “개발자가 직접 설계하는 것”이라는 걸 확실히 배움.

---

## 7) 주문 상태(State) 개념 부족 → 상태 기반 구조로 완성

> **문제**
> 아무 때나 주문을 수정하거나 취소할 수 있다고 생각함.
>
> **해결**
>
> * NEW / CANCELLED / SHIPPED / COMPLETED 상태 추가
> * 상태에 따라 수정/취소 제한
>
> **배운 점**
> 주문은 CRUD가 아니라 “상태 기반(Stateful) 도메인”이라는 것을 이해함.

---

## 8) JOIN 매핑 실패 → resultMap 제대로 작성해서 해결

> **문제**
> JOIN한 결과가 객체에 정확히 들어가지 않음.
>
> **해결**
>
> * Order / OrderItems / Items 각각의 매핑 구조를 분석해 resultMap 재설계
>
> **배운 점**
> MyBatis는 ORM이 아니므로 JOIN 매핑을 반드시 직접 설계해야 한다는 사실.

---

## 9) DTO 필요성 제대로 이해

> **문제**
> Entity를 그대로 API 응답에 사용함.
>
> **해결**
>
> * DTO로 필요한 필드만 전달
> * Entity는 외부에 절대 노출되지 않도록 변경
>
> **배운 점**
> DTO는 보안, 유지보수, 명확한 API를 위해 필수.

---

## 10) 테스트 간 간섭 문제 → DB 초기화 전략으로 해결

> **문제**
> 이전 테스트 데이터가 남아서 예상 값이 계속 어긋남.
>
> **해결**
>
> * @BeforeEach에서 deleteAll() 실행
> * 모든 테스트를 완전히 독립적으로 보장
>
> **배운 점**
> 테스트는 절대 서로 간섭되면 안 된다.

---

## 11) 테스트 실행 시 DB에 계속 데이터 쌓임 → @Transactional로 해결

> **문제**
> 테스트할 때마다 insert된 데이터가 남아서 id가 증가하고 중복 예외 발생.
>
> **해결**
>
> * 테스트 코드에 @Transactional 적용
> * 테스트 끝나면 자동 rollback
>
> **배운 점**
> 테스트에도 트랜잭션을 적용해야 “테스트를 마음껏 반복 가능”하다는 걸 알게 됨.

---

# 📈 프로젝트를 통해 배운 점 (최종 정리)

* Controller는 얇게, Service가 핵심을 담당하는 실무 구조를 제대로 이해함
* 예외 처리 흐름 (AppException → ErrorCode → Global Handler) 완전히 체득
* MyBatis JOIN, nested resultMap, association/collection 매핑 능력 향상
* 상태 기반 주문 도메인의 흐름을 실제로 구현하며 실무 감각 습득
* 트랜잭션 rollback 개념을 단순 이론이 아닌 실제 상황에서 체감
* DTO가 왜 필요한지, 어떻게 API 안정성과 보안을 높이는지 명확히 이해
* 통합 테스트에서 “DB 초기화 + rollback”의 필요성을 경험적으로 학습
* 단순 CRUD가 아니라 실제 쇼핑몰 주문/재고 구조를 모델링해보며 실전 감각 확립

```

---


