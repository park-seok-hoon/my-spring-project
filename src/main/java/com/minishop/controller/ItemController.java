package com.minishop.controller;

import com.minishop.domain.Items;
import com.minishop.dto.item.ItemCreateRequest;
import com.minishop.dto.item.ItemUpdateRequest;
import com.minishop.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<Items>> createItem(@Valid @RequestBody ItemCreateRequest request) {
        Items newItem = itemService.save(request);
        return ResponseEntity.ok(ApiResponse.success("상품 등록 성공", newItem));
    }

    /**
     * ✅ 상품 전체 조회 (Read All)
     * 상품이 없으면 AppException에서 ITEM_NOT_FOUND 발생
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Items>>> getAllItems() {
        List<Items> items = itemService.findAll();
        return ResponseEntity.ok(ApiResponse.success("상품 목록 조회 성공",items)); // 예외 발생 시 GlobalExceptionHandler에서 처리
    }

    /**
     * ✅ 상품 단건 조회 (Read One)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Items>> getItem(@PathVariable Long id) {
        Items item = itemService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("상품 조회 성공",item));
    }

    /**
     * ✅ 상품 수정 (Update)
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Items>> updateItem(@PathVariable Long id, @Valid @RequestBody ItemUpdateRequest updateRequest) {
        Items updatedItem= itemService.update(id, updateRequest);
        return ResponseEntity.ok(ApiResponse.success("상품이 성공적으로 수정되었습니다.", updatedItem));
    }

    /**
     * ✅ 상품 삭제 (Delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        itemService.delete(id);

        return ResponseEntity.ok(ApiResponse.success("상품 삭제 성공",null));
    }

    /**
     * ✅ 테스트용 예외 (임의 호출)
     */
    @GetMapping("/error-ex")
    public void errorEx() {
        throw new RuntimeException("테스트용 예외 발생!");
    }
}
