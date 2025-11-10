# 🛠 MiniShop API 리팩토링 (Controller 응답 통일화)

## 📌 리팩토링 목적
기존에는 Controller에서 예외 처리, 메시지 구성, 로직 일부를 직접 수행했으나,  
이번 리팩토링을 통해 **Controller는 응답만 담당하고**,  
**Service에서 모든 비즈니스 로직과 검증을 처리**하도록 구조를 개선했습니다.

이를 통해:
- Controller의 책임 단일화 (SRP 원칙 적용)
- 중복 코드 제거 및 가독성 향상
- API 응답 포맷 통일 (`{ code, message, data }`)

---

## ⚙️ 리팩토링 전 문제점

```java
// ✅ 상품 등록 (Create)
@PostMapping
public ResponseEntity<Items> createItem(@Valid @RequestBody ItemCreateRequest request) {

    Items saved = new Items();
    saved.setName(request.getName());
    saved.setPrice(request.getPrice());
    saved.setStockQuantity(request.getStockQuantity());

    Items newItem = itemService.save(saved);

    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(newItem.getId())
            .toUri();

    return ResponseEntity.created(location).body(newItem);
}

// ✅ 상품 수정 (Update)
@PutMapping("/{id}")
public ResponseEntity<String> updateItem(@PathVariable Long id, @Valid @RequestBody ItemUpdateRequest request) {
    Items updatedItem = new Items();
    updatedItem.setId(id);
    updatedItem.setName(request.getName());
    updatedItem.setPrice(request.getPrice());
    updatedItem.setStockQuantity(request.getStockQuantity());

    int updateCount = itemService.update(id, updatedItem);
    log.info("수정된 행 수 = {}", updateCount);

    return ResponseEntity.ok("상품이 성공적으로 수정되었습니다.");
}
```

## ❌ Controller에서
- 검증 로직 수행
- 문자열로 직접 응답 생성
→ 유지보수가 어렵고 코드 일관성이 떨어졌음.

✅ 리팩토링 후 구조
## 📍 Controller (응답만 담당)
```java
// ✅ 상품 등록
@PostMapping
public ResponseEntity<ApiResponse<Items>> createItem(@Valid @RequestBody ItemCreateRequest request) {
    Items newItem = itemService.save(request);
    return ResponseEntity.ok(ApiResponse.success("상품 등록 성공", newItem));
}

// ✅ 상품 수정
@PutMapping("/{id}")
public ResponseEntity<ApiResponse<Items>> updateItem(@PathVariable Long id, @Valid @RequestBody ItemUpdateRequest updateRequest) {
    Items updatedItem = itemService.update(id, updateRequest);
    return ResponseEntity.ok(ApiResponse.success("상품이 성공적으로 수정되었습니다.", updatedItem));
}
```
✅ Controller는 요청을 받고, ApiResponse 형식으로 응답만 반환.
모든 검증과 예외는 Service 계층으로 위임.

## 📍 Service (검증 + 예외 처리 + DB 연동)
```java
     // ✅ 상품 등록
public Items save(ItemCreateRequest request) {

    if (request.getPrice() <= 0)
        throw new AppException(ErrorCode.INVALID_PRICE);

    if (request.getStockQuantity() < 0)
        throw new AppException(ErrorCode.INVALID_STOCK);

    if (itemRepository.findByName(request.getName()) != null)
        throw new AppException(ErrorCode.DUPLICATE_ITEM, "상품명: " + request.getName());

    Items item = new Items();
    item.setName(request.getName());
    item.setPrice(request.getPrice());
    item.setStockQuantity(request.getStockQuantity());

    return itemRepository.save(item);
}

// ✅ 상품 수정
public Items update(Long id, ItemUpdateRequest request) {
    // (1) 존재하지 않는 상품인지 체크
    Items existedItem = itemRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND, "수정할 상품(id=" + id + ")이 없습니다."));

    // (2) 가격 검증
    if (request.getPrice() <= 0)
        throw new AppException(ErrorCode.INVALID_PRICE, "수정할 가격: " + request.getPrice());

    // (3) 재고 검증
    if (request.getStockQuantity() < 0)
        throw new AppException(ErrorCode.INVALID_STOCK, "수정할 재고 수량: " + request.getStockQuantity());

    // (4) 상품명 중복 확인 (단, 이름이 변경될 때만 검사)
    if (!existedItem.getName().equals(request.getName()) &&
            itemRepository.findByName(request.getName()) != null)
        throw new AppException(ErrorCode.DUPLICATE_ITEM, "상품명: " + request.getName());

    Items updateItem = new Items();
    updateItem.setName(request.getName());
    updateItem.setPrice(request.getPrice());
    updateItem.setStockQuantity(request.getStockQuantity());

    int result = itemRepository.update(id, updateItem);

    if (result == 0)
        throw new AppException(ErrorCode.DATABASE_ERROR);

    return itemRepository.findById(id)
            .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND, "수정 후 상품을 찾을 수 없습니다."));
}
```
✅ 비즈니스 로직, 유효성 검증, 예외 처리 모두 Service에서 수행
✅ Controller는 결과만 받아 응답

### 📦 ApiResponse (응답 통일화) 
```java
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String code;    //상태 코드(SUCCESS, ERROR 등)
    private String message; //설명 메시지
    private T data;         //응답 데이터 (성공 시만 포함)

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "요청이 성공적으로 처리되었습니다.",data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS",message,data);

    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(code,message,null);
    }

}
```

📍 GlobalExceptionHandler (예외 통합 처리)
```java
@ExceptionHandler(AppException.class)
public ResponseEntity<ApiResponse<Void>> handleAppException(AppException e) {
    ErrorCode errorCode = e.getErrorCode();
    return ResponseEntity
            .status(errorCode.getStatus())
            .body(ApiResponse.error(errorCode.name(), e.getMessage()));
}
```
✅ 예외 발생 시 통일된 JSON 응답 반환
✅ 예외별로 다른 HTTP 상태 코드 적용 가능

### 🧩 DTO를 Service에 직접 전달한 이유
| 항목                   | 설명                                                       |
| -------------------- | -------------------------------------------------------- |
| **1. 단일 책임 원칙(SRP)** | Controller는 요청·응답만, Service는 로직·검증만 담당하도록 분리             |
| **2. 유지보수성 향상**      | 검증 로직을 Service에 둬서 Controller 수정 없이 재사용 가능               |
| **3. DTO의 의도 명확화**   | DTO는 "요청 전용 객체"로, 엔티티와 명확히 분리됨                           |
| **4. 테스트 용이성**       | 단위 테스트 시 DTO를 그대로 전달 가능                                  |
| **5. 계층 간 의존성 명확화**  | Controller → Service 단방향 구조 유지 (Service는 Controller를 모름) |


📤 응답 예시

✅ 성공 시
```
{
  "code": "SUCCESS",
  "message": "상품 등록 성공",
  "data": {
    "id": 101,
    "name": "맥북 프로",
    "price": 2500000,
    "stockQuantity": 5
  }
}
```

❌ 실패 시 (상품 중복)
```
{
  "code": "DUPLICATE_ITEM",
  "message": "상품명: 맥북 프로",
  "data": null
}
```

❌ 실패 시 (검증 실패)
```
{
  "code": "VALIDATION_ERROR",
  "message": "가격은 0보다 커야 합니다.",
  "data": null
}
```

### 🧠 리팩토링 결과 요약
| 구분            | 리팩토링 전                 | 리팩토링 후                             |
| ------------- | ---------------------- | ---------------------------------- |
| 예외 처리         | Controller에서 직접 처리     | ✅ Service + GlobalExceptionHandler |
| 응답 포맷         | 문자열 / 불규칙              | ✅ `{ code, message, data }` 통일     |
| Controller 역할 | 로직 + 응답 + 검증           | ✅ 응답 전담                            |
| DTO 처리        | Controller에서 Entity 변환 | ✅ DTO를 Service로 전달 (Service에서 변환)  |
| 유지보수성         | 낮음                     | ✅ 높음                               |



### 📁 패키지 구조
minishop
 ┣ 📂controller
 ┃ ┗ ItemController.java
 ┣ 📂service
 ┃ ┗ ItemService.java
 ┣ 📂exception
 ┃ ┣ AppException.java
 ┃ ┣ ErrorCode.java
 ┃ ┗ GlobalExceptionHandler.java
 ┣ 📂response
 ┃ ┗ ApiResponse.java
 ┗ 📂dto
   ┣ ItemCreateRequest.java
   ┗ ItemUpdateRequest.java


### 🏁 결론
- Controller는 단순히 응답만 담당하고,
- Service는 검증 및 예외 처리를 전담하며,
- 모든 API 응답이 표준화된 구조로 통일되었습니다.

💬 “Controller는 응답만, Service는 처리만”
이 원칙에 따라 구조적 안정성과 유지보수성을 확보했습니다.
