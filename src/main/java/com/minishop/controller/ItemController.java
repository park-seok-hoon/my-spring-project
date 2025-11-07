package com.minishop.controller;

import com.minishop.domain.Items;
import com.minishop.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    /**
     * 상품 등록 (Create)
     */
    @PostMapping
    public ResponseEntity<?> createItem(@RequestBody Items item) {
        try {
            // 서비스 호출
            Items savedItem = itemService.save(item);

            // 생성된 자원 URI 추가
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(savedItem.getId())
                    .toUri();

            return ResponseEntity.created(location).body(savedItem); // 201 Created
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("잘못된 요청입니다: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("[createItem] 예외 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("상품 등록 중 오류가 발생했습니다.");
        }
    }

    /**
     * 상품 수정 (Update)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable("id") Long id, @RequestBody Items items) {
        try {
            items.setId(id);
            int updatedRows = itemService.update(id, items);
            log.info("바뀐 행의 수 = {}", updatedRows);

            if (updatedRows == 0) {
                return ResponseEntity.notFound().build(); // 404 Not Found
            }

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("상품이 성공적으로 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("요청 데이터가 잘못되었습니다: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("[updateItem] 예외 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("상품 수정 중 오류가 발생했습니다.");
        }
    }

    /**
     * 상품 삭제 (Delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable("id") Long id) {
        try {
            int deletedRows = itemService.delete(id);
            if (deletedRows == 0) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("삭제할 상품을 찾을 수 없습니다. (id=" + id + ")");
            }

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body("상품이 삭제되었습니다.");
        } catch (RuntimeException e) {
            log.error("[deleteItem] 예외 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("상품 삭제 중 오류가 발생했습니다.");
        }
    }

    /**
     * 상품 단건 조회 (Read)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable("id") Long id) {
        try {
            Items item = itemService.findById(id);
            if (item == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("상품을 찾을 수 없습니다. (id=" + id + ")");
            }
            return ResponseEntity.ok(item); // 200 OK
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("요청이 잘못되었습니다: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("[getById] 예외 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("상품 조회 중 오류가 발생했습니다.");
        }
    }

    /**
     * 전체 상품 조회 (Read All)
     */
    @GetMapping
    public ResponseEntity<?> getAllItems() {
        try {
            List<Items> items = itemService.findAll();
            if (items.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body("등록된 상품이 없습니다."); // 200 OK
            }
            return ResponseEntity.ok(items); // 정상 조회
        } catch (Exception e) {
            log.error("[getAllItems] 예외 발생", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("상품 목록 조회 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 예외 테스트용
     */
    @GetMapping("/error-ex")
    public void errorEx() {
        throw new RuntimeException("테스트용 예외 발생!");
    }
}
