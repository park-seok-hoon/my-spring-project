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
