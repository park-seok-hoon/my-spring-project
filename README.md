# 🧩 MiniShop — Service 계층 예외 처리 버전

이 버전은 **Controller에서는 예외를 직접 처리하지 않고**,  
모든 검증 및 예외를 **Service 계층과 GlobalExceptionHandler에서 일괄 처리**하도록 개선한 구조입니다.  

Controller는 요청과 응답에만 집중하고,  
Service 계층이 비즈니스 로직 + 예외 검증을 담당합니다.  
이는 실무에서 가장 권장되는 **Spring 예외 처리 표준 패턴**입니다.

---

## ⚙️ 기술 스택
- **Language**: Java 17  
- **Framework**: Spring Boot  
- **Database**: H2 (또는 MySQL)  
- **ORM / Mapper**: MyBatis  
- **Validation**: `jakarta.validation` (`@Valid`)  
- **Build Tool**: Gradle  
- **IDE**: IntelliJ IDEA  

---

## 📁 패키지 구조
com.minishop
```
├── controller # 요청 처리 및 응답 반환 (예외는 처리하지 않음)
├── service # 비즈니스 로직 및 예외 처리
├── repository # MyBatis Mapper 연동
├── domain # Entity 클래스
├── dto.item # 요청 DTO (ItemCreateRequest, ItemUpdateRequest)
└── exception # 공통 예외 클래스 및 GlobalExceptionHandler
```

---

## 💻 Controller (ItemController.java)

```java
package com.minishop.controller;

import com.minishop.domain.Items;
import com.minishop.dto.item.ItemCreateRequest;
import com.minishop.dto.item.ItemUpdateRequest;
import com.minishop.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    /**
     * ✅ 상품 등록 (Create)
     * 예외는 ItemService에서 AppException으로 던지고,
     * GlobalExceptionHandler에서 처리됨.
     */
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

    /**
     * ✅ 상품 전체 조회 (Read All)
     */
    @GetMapping
    public ResponseEntity<List<Items>> getAllItems() {
        List<Items> items = itemService.findAll();
        return ResponseEntity.ok(items); // 예외는 Handler에서 처리
    }

    /**
     * ✅ 상품 단건 조회 (Read One)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Items> getItem(@PathVariable Long id) {
        Items item = itemService.findById(id);
        return ResponseEntity.ok(item);
    }

    /**
     * ✅ 상품 수정 (Update)
     */
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

    /**
     * ✅ 상품 삭제 (Delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.ok("상품이 성공적으로 삭제되었습니다.");
    }

    /**
     * ✅ 테스트용 예외
     */
    @GetMapping("/error-ex")
    public void errorEx() {
        throw new RuntimeException("테스트용 예외 발생!");
    }
}
```

## 💻 Service (ItemService.java)
```
package com.minishop.service;

import com.minishop.domain.Items;
import com.minishop.exception.*;
import com.minishop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public Items save(Items item) {
        if (item.getPrice() <= 0) {
            throw new AppException(ErrorCode.INVALID_PRICE);
        }

        if (item.getStockQuantity() < 0) {
            throw new AppException(ErrorCode.INVALID_STOCK);
        }

        if (itemRepository.findByName(item.getName()) != null) {
            throw new AppException(ErrorCode.DUPLICATE_ITEM, "상품명: " + item.getName());
        }

        return itemRepository.save(item);
    }

    public void delete(Long id) {
        int deletedRows = itemRepository.delete(id);
        if (deletedRows == 0) {
            throw new AppException(ErrorCode.ITEM_NOT_FOUND, "삭제할 상품(id=" + id + ")이 없습니다.");
        }
    }

    public Items findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND, "상품 ID: " + id));
    }

    public List<Items> findAll() {
        List<Items> items = itemRepository.findAll();
        if (items.isEmpty()) {
            throw new AppException(ErrorCode.ITEM_NOT_FOUND, "등록된 상품이 없습니다.");
        }
        return items;
    }

    public int update(Long id, Items items) {
        Items existedItem = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND, "수정할 상품(id=" + id + ")이 없습니다."));

        if (items.getPrice() <= 0) {
            throw new AppException(ErrorCode.INVALID_PRICE, "수정할 가격: " + items.getPrice());
        }

        if (items.getStockQuantity() < 0) {
            throw new AppException(ErrorCode.INVALID_STOCK, "수정할 재고 수량: " + items.getStockQuantity());
        }

        if (!existedItem.getName().equals(items.getName()) &&
                itemRepository.findByName(items.getName()) != null) {
            throw new AppException(ErrorCode.DUPLICATE_ITEM, "상품명: " + items.getName());
        }

        return itemRepository.update(id, items);
    }
}

```
## ⚙️ 예외 처리 클래스

## 🧱 AppException.java
```
package com.minishop.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {

    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public HttpStatus getStatus() {
        return this.errorCode.getStatus();
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
```

## 🧩 ErrorCode.java
```
package com.minishop.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),
    DUPLICATE_ITEM(HttpStatus.CONFLICT, "동일한 상품이 이미 존재합니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "가격이 올바르지 않습니다."),
    INVALID_STOCK(HttpStatus.BAD_REQUEST, "재고 수량이 유효하지 않습니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 오류가 발생했습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
```

## 📘 ErrorResult.java
```
package com.minishop.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResult {
    private String code;
    private String message;
}
```

## ⚙️ GlobalExceptionHandler.java
```
package com.minishop.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResult> handleAppException(AppException e) {
        log.warn("[AppException] {}", e.getMessage());
        ErrorResult error = new ErrorResult(e.getErrorCode().name(), e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(error);
    }

    // 기타 모든 예외 (서버 내부 오류)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResult> handleException(Exception e) {
        log.error("[Exception] {}", e.getMessage(), e);
        ErrorResult error = new ErrorResult("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
        return ResponseEntity.internalServerError().body(error);
    }

    // @Valid 검증 실패 시
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResult> handleValidationExceptions(MethodArgumentNotValidException e) {

        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("입력값이 올바르지 않습니다.");

        log.warn("[ValidationException] {}", errorMessage);

        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        for (FieldError fe : fieldErrors) {
            log.warn("필드 에러: field={}, message={}", fe.getField(), fe.getDefaultMessage());
        }

        ErrorResult error = new ErrorResult("BAD_REQUEST", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

## 📦 DTO 구조 예시
## 📝 ItemCreateRequest.java
```
package com.minishop.dto.item;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCreateRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    @Min(value = 1, message = "가격은 1원 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "재고 수량은 필수 입력 값입니다.")
    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    private Integer stockQuantity;
}
```

## 📝 ItemUpdateRequest.java
```
package com.minishop.dto.item;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemUpdateRequest {

    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    @Min(value = 1, message = "가격은 1원 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "재고 수량은 필수 입력 값입니다.")
    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
    private Integer stockQuantity;
}
```

## 📘 예외 응답 예시
```
{
  "code": "INVALID_PRICE",
  "message": "가격은 1원 이상이어야 합니다."
}
```

## ✅ 구조 개선 요약
| 항목                         | 설명                                                  |
| -------------------------- | --------------------------------------------------- |
| **Controller 역할**          | 요청/응답만 담당 (`try-catch` 없음)                          |
| **Service 역할**             | 검증 및 예외 발생 (`AppException`)                         |
| **GlobalExceptionHandler** | 예외를 JSON 형식으로 일괄 처리                                 |
| **Validation**             | `@Valid`와 `MethodArgumentNotValidException`으로 입력 검증 |
| **응답 포맷 통일**               | 모든 에러가 `{ code, message }` 형태로 반환                   |





