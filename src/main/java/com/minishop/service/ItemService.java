package com.minishop.service;

import com.minishop.domain.Items;
import com.minishop.dto.item.ItemCreateRequest;
import com.minishop.dto.item.ItemUpdateRequest;
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

    public Items save(ItemCreateRequest request) {

        if(request.getPrice() <= 0 ) {
            throw new AppException(ErrorCode.INVALID_PRICE);
        }

        if(request.getStockQuantity() < 0 ){
            throw new AppException(ErrorCode.INVALID_STOCK);
        }

        if(itemRepository.findByName(request.getName()) != null){
            throw new AppException(ErrorCode.DUPLICATE_ITEM, "상품명: " + request.getName());
        }

        //유효성 검사+예외 처리 완료 후 저장
        Items item = new Items();
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        item.setStockQuantity(request.getStockQuantity());


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


    public Items update(Long id, ItemUpdateRequest request) {
        // (1) 존재하지 않는 상품인지 체크
        Items existedItem = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND, "수정할 상품(id=" + id + ")이 없습니다."));


        // (2) 가격 검증
        if (request.getPrice() <= 0) {
            throw new AppException(ErrorCode.INVALID_PRICE, "수정할 가격: " + request.getPrice());
        }

        // (3) 재고 검증
        if (request.getStockQuantity() < 0) {
            throw new AppException(ErrorCode.INVALID_STOCK, "수정할 재고 수량: " + request.getStockQuantity());
        }

        // (4) 상품명 중복 확인 (단, 이름이 변경될 때만 검사)
        if (!existedItem.getName().equals(request.getName()) &&
                itemRepository.findByName(request.getName()) != null) {
            throw new AppException(ErrorCode.DUPLICATE_ITEM, "상품명: " + request.getName());
        }

        //유효성 검사+예외 처리 완료 후 수정
        Items updateItem = new Items();
        updateItem.setName(request.getName());
        updateItem.setPrice(request.getPrice());
        updateItem.setStockQuantity(request.getStockQuantity());

        //DB 업데이트 진행
        int result =itemRepository.update(id,updateItem);

        if(result==0){
            throw new AppException(ErrorCode.DATABASE_ERROR);
        }

        // 모든 검증 통과 → DB 업데이트 진행
        return itemRepository.findById(id)
                .orElseThrow(() ->
                        new AppException(ErrorCode.ITEM_NOT_FOUND, "수정 후 상품을 찾을 수 없습니다."));
    }
}
