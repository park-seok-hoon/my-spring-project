package com.minishop.service;

import com.minishop.domain.Items;
import com.minishop.dto.item.ItemCreateRequest;
import com.minishop.dto.item.ItemUpdateRequest;
import com.minishop.exception.AppException;
import com.minishop.exception.ErrorCode;
import com.minishop.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceTest {

    @Autowired
    ItemService itemService;

    @Autowired
    ItemRepository itemRepository;

    @BeforeEach
    void clearDB() {
        itemRepository.deleteAll();
    }

    @Test
    @DisplayName("상품 등록 성공")
    void saveSuccess() {
        ItemCreateRequest req = new ItemCreateRequest("운동화", 30000, 10);

        Items saved = itemService.save(req);

        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("운동화");
        assertThat(saved.getPrice()).isEqualTo(30000);
        assertThat(saved.getStockQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("상품 등록 실패 - 가격이 0 이하")
    void saveFail_InvalidPrice() {
        ItemCreateRequest req = new ItemCreateRequest("운동화", 0, 10);

        assertThatThrownBy(() -> itemService.save(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("가격이 올바르지 않습니다");
    }

    @Test
    @DisplayName("상품 등록 실패 - 재고가 음수")
    void saveFail_InvalidStock() {
        ItemCreateRequest req = new ItemCreateRequest("운동화", 10000, -1);

        assertThatThrownBy(() -> itemService.save(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("재고 수량이 유효하지 않습니다.");
    }

    @Test
    @DisplayName("상품 등록 실패 - 상품명 중복")
    void saveFail_DuplicateName() {
        itemService.save(new ItemCreateRequest("운동화", 30000, 10));

        ItemCreateRequest req = new ItemCreateRequest("운동화", 20000, 5);

        assertThatThrownBy(() -> itemService.save(req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("상품명");
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void deleteSuccess() {
        Items saved = itemService.save(new ItemCreateRequest("운동화", 30000, 10));

        itemService.delete(saved.getId());

        assertThat(itemRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("상품 삭제 실패 - 존재하지 않는 상품")
    void deleteFail_NotFound() {

        assertThatThrownBy(() -> itemService.delete(99999L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("삭제할 상품");
    }

    @Test
    @DisplayName("상품 단건 조회 성공")
    void findByIdSuccess() {
        Items saved = itemService.save(new ItemCreateRequest("운동화", 30000, 10));

        Items found = itemService.findById(saved.getId());

        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("운동화");
    }

    @Test
    @DisplayName("상품 단건 조회 실패 - 존재하지 않음")
    void findByIdFail_NotFound() {

        assertThatThrownBy(() -> itemService.findById(99999L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("상품 ID");
    }

    @Test
    @DisplayName("상품 전체 조회 성공")
    void findAllSuccess() {
        itemService.save(new ItemCreateRequest("운동화", 30000, 10));
        itemService.save(new ItemCreateRequest("모자", 15000, 20));

        List<Items> list = itemService.findAll();

        assertThat(list).hasSize(2);
    }

    @Test
    @DisplayName("상품 전체 조회 실패 - 상품이 없음")
    void findAllFail_Empty() {

        assertThatThrownBy(() -> itemService.findAll())
                .isInstanceOf(AppException.class)
                .hasMessageContaining("등록된 상품이 없습니다");
    }

    // ========================================
    // UPDATE 테스트
    // ========================================

    @Test
    @DisplayName("상품 수정 성공")
    void updateSuccess() {
        Items saved = itemService.save(new ItemCreateRequest("운동화", 30000, 10));

        ItemUpdateRequest req = new ItemUpdateRequest("러닝화", 45000, 5);

        Items updated = itemService.update(saved.getId(), req);

        assertThat(updated.getName()).isEqualTo("러닝화");
        assertThat(updated.getPrice()).isEqualTo(45000);
        assertThat(updated.getStockQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("상품 수정 실패 - 존재하지 않는 상품")
    void updateFail_ItemNotFound() {

        ItemUpdateRequest req = new ItemUpdateRequest("운동화", 20000, 10);

        assertThatThrownBy(() -> itemService.update(99999L, req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("수정할 상품");
    }

    @Test
    @DisplayName("상품 수정 실패 - 가격 0 이하")
    void updateFail_InvalidPrice() {

        Items saved = itemService.save(new ItemCreateRequest("운동화", 30000, 10));

        ItemUpdateRequest req = new ItemUpdateRequest("운동화", 0, 10);

        assertThatThrownBy(() -> itemService.update(saved.getId(), req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("수정할 가격");
    }

    @Test
    @DisplayName("상품 수정 실패 - 재고 음수")
    void updateFail_InvalidStock() {

        Items saved = itemService.save(new ItemCreateRequest("운동화", 30000, 10));

        ItemUpdateRequest req = new ItemUpdateRequest("운동화", 10000, -5);

        assertThatThrownBy(() -> itemService.update(saved.getId(), req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("재고");
    }

    @Test
    @DisplayName("상품 수정 실패 - 상품명 중복")
    void updateFail_DuplicateName() {

        itemService.save(new ItemCreateRequest("운동화", 30000, 10));
        Items saved2 = itemService.save(new ItemCreateRequest("모자", 15000, 20));

        ItemUpdateRequest req = new ItemUpdateRequest("운동화", 20000, 5);

        assertThatThrownBy(() -> itemService.update(saved2.getId(), req))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("상품명");
    }
}
