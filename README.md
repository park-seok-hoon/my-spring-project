# 🧩 예외 처리 구조 변경 (Controller → Service 중심)

## 📖 변경 배경
기존에는 Controller 단에서 예외를 처리했지만,  
이 방식은 **비즈니스 로직과 요청 처리 로직이 섞여 유지보수가 어렵고 재사용성이 떨어지는 문제**가 있었습니다.  

그래서 예외 처리의 책임을 **Service 계층으로 이동**시켜  
Controller는 요청과 응답만 담당하고,  
실제 검증과 예외 발생은 Service에서 수행하도록 변경했습니다.

---

## ✅ 변경 이유 요약

| 구분 | Controller 예외 처리 방식 | Service 예외 처리 방식 |
|------|---------------------------|------------------------|
| 책임 분리 | 요청 처리 + 검증이 섞임 | 역할 명확 (Controller는 응답만) |
| 코드 유지보수 | 각 Controller마다 예외 중복 | Service에서 통합 관리 |
| 재사용성 | 동일 검증을 여러 API에서 재사용 불가 | 하나의 Service 로직으로 재활용 가능 |
| 실무 일치도 | 학습용 수준 | 실무 프로젝트 구조와 동일 |

---

## ⚙️ 구조 개요
```
com.minishop
├── controller
│ └── ItemController.java # 요청·응답 담당
├── service
│ └── ItemService.java # 비즈니스 로직 + 예외 처리
├── exception
│ ├── AppException.java # 공통 예외 클래스
│ ├── ErrorCode.java # 예외 코드 정의
│ ├── ErrorResult.java # 응답 DTO
│ └── GlobalExceptionHandler.java # 전역 예외 처리
└── domain
└── Items.java
```


---

## 🧠 예외 처리 흐름

1. 클라이언트가 상품 등록 요청을 보냄  
2. `Controller`가 요청을 `Service`로 전달  
3. `Service`에서 가격, 재고, 중복 상품 검증 수행  
4. 조건 불일치 시 `throw new AppException(ErrorCode.XXX)` 발생  
5. `GlobalExceptionHandler`가 예외를 감지하고 JSON 응답 반환  

---

## 💻 코드 전체

### 📂 `ErrorCode.java`
```java
package com.minishop.exception;

import org.springframework.http.HttpStatus;

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

    public HttpStatus getStatus() { return status; }
    public String getMessage() { return message; }
}
```

### 📂 AppException.java
```
package com.minishop.exception;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // RuntimeException의 message 필드에 기본 메시지 전달
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

### 📂 ErrorResult.java
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

### 📂 GlobalExceptionHandler.java
```
package com.minishop.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
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

    // 그 외 모든 예외 (시스템 에러)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResult> handleException(Exception e) {
        log.error("[Exception] {}", e.getMessage(), e);
        ErrorResult error = new ErrorResult("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");
        return ResponseEntity.internalServerError().body(error);
    }


    //@Valid 검증에서 실패 했을 경우에 발생하는 예외를 잡아서 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResult> handleValidationExceptions(MethodArgumentNotValidException e) {

        // 첫 번째 필드 에러 메시지 추출
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("입력값이 올바르지 않습니다.");

        log.warn("[ValidationException] {}", errorMessage);


        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        for (FieldError fe : fieldErrors) {
            log.warn("필드 에러 표시 field={}, message={}", fe.getField(), fe.getDefaultMessage());
        }

        // 응답 DTO 생성
        ErrorResult error = new ErrorResult(
                "BAD_REQUEST",
                errorMessage
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }



}
```

### 📂 ItemController.java
```
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
     * 상품 등록 (Create)
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
     * 상품 전체 조회 (Read All)
     * 상품이 없으면 AppException에서 ITEM_NOT_FOUND 발생
     */
    @GetMapping
    public ResponseEntity<List<Items>> getAllItems() {
        List<Items> items = itemService.findAll();
        return ResponseEntity.ok(items); // 예외 발생 시 GlobalExceptionHandler에서 처리
    }

    /**
     * 상품 단건 조회 (Read One)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Items> getItem(@PathVariable Long id) {
        Items item = itemService.findById(id);
        return ResponseEntity.ok(item);
    }

    /**
     * 상품 수정 (Update)
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
     * 상품 삭제 (Delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteItem(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.ok("상품이 성공적으로 삭제되었습니다.");
    }

    /**
     * 테스트용 예외 (임의 호출)
     */
    @GetMapping("/error-ex")
    public void errorEx() {
        throw new RuntimeException("테스트용 예외 발생!");
    }
}
```

### 📂 ItemService.java
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

    //인터페이스에 대해서만 알고 있어도 스프링에서 자동으로 해당 구현체로 연결해줌으로 신경을 쓰지 않아도 됨.
    private final ItemRepository itemRepository;

    public Items save(Items item) {
        if(item.getPrice() <= 0 ) {
            throw new AppException(ErrorCode.INVALID_PRICE);
        }

        if(item.getStockQuantity() < 0 ){
            throw new AppException(ErrorCode.INVALID_STOCK);
        }

        if(itemRepository.findByName(item.getName()) != null){
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
        // (1) 존재하지 않는 상품인지 체크
        Items existedItem = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND, "수정할 상품(id=" + id + ")이 없습니다."));

        // (2) 가격 검증
        if (items.getPrice() <= 0) {
            throw new AppException(ErrorCode.INVALID_PRICE, "수정할 가격: " + items.getPrice());
        }

        // (3) 재고 검증
        if (items.getStockQuantity() < 0) {
            throw new AppException(ErrorCode.INVALID_STOCK, "수정할 재고 수량: " + items.getStockQuantity());
        }

        // (4) 상품명 중복 확인 (단, 이름이 변경될 때만 검사)
        if (!existedItem.getName().equals(items.getName()) &&
                itemRepository.findByName(items.getName()) != null) {
            throw new AppException(ErrorCode.DUPLICATE_ITEM, "상품명: " + items.getName());
        }

        // 모든 검증 통과 → DB 업데이트 진행
        return itemRepository.update(id, items);
    }
}
```

### 📘 설계 의도 요약
이 구조는 Controller는 요청과 응답에만 집중하고,
Service가 핵심 검증 및 예외 처리를 담당하도록 분리된 구조입니다.

이렇게 함으로써 코드의 책임이 명확해지고,
유지보수성과 확장성이 높아집니다.

또한 GlobalExceptionHandler를 통해
모든 예외를 일관된 JSON 형식으로 반환하여
클라이언트가 예외를 처리하기 쉽도록 만들었습니다.

🧾 예시 응답

### ✅ 정상 요청
{
  "id": 1,
  "name": "노트북",
  "price": 1500000,
  "stockQuantity": 10
}

### ❌ 예외 발생 (중복 상품명)
{
  "code": "DUPLICATE_ITEM",
  "message": "상품명: 노트북"
}

### 🧩 유효성 검사(@Valid)와 예외 처리 연동에 대한 배움
Controller에서 @Valid를 사용하면,
Spring이 컨트롤러 진입 전에 유효성 검증을 수행하며
조건에 맞지 않으면 MethodArgumentNotValidException을 자동으로 던진다는 것을 배웠다.

따라서 GlobalExceptionHandler에서 해당 예외를 잡아주기 위해
@ExceptionHandler(MethodArgumentNotValidException.class)를 추가해야
유효성 검증 실패 시에도 일관된 JSON 응답을 반환할 수 있다는 점을 알게 됐다.


### 예상 요청
{
    "code": "BAD_REQUEST",
    "message": "가격은 1원 이상이어야 합니다."
}

### 실제 요청 결과
{
    "code": "INTERNAL_SERVER_ERROR",
    "message": "서버 내부 오류가 발생했습니다."
}





