package com.minishop.repository;

import com.minishop.domain.item.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Optional<Item> findById(Long id);
    Item save(Item item);
    int delete(Long id);
    List<Item> findAll();
    Item findByName(String itemName);
    void update(Long id, Item updateItem);
    void deleteAll();



    /* 마이 바티스에서는 이렇게 사용하여 가져와야 되지만
       JPA에서는 em.find()로 가져온 영속 상태 엔티티의 필드를 바꾸면 **더티 체킹(Dirty Checking)**으로 자동 UPDATE가 나가기 때문에,
       명시적으로 "update 쿼리를 실행"할 필요 자체가 없음
    //    int update(Long id, Item items);
    //    void update(Item dbItem);
    */
}
