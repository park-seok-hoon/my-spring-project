//package com.minishop.repository.mybatis.mapper;
//
//import com.minishop.domain.Items;
//import com.minishop.domain.item.Item;
//import org.apache.ibatis.annotations.Mapper;
//import org.apache.ibatis.annotations.Param;
//
//import java.util.List;
//
//@Mapper
//public interface ItemMapper {
//
//    Item findById(Long id);
//    List<Item> findAll(); //READ
//    void saveItem(Item item); //CREATE
//    int updateItem(@Param("id") Long id,@Param("item") Item item); //UPDATE
//    int deleteItem(Long id); //DELETE
//    Item findByName(String itemName);  //예외 처리를 위한 아이템 이름 가져오기
//    int update(Item dbItem);
//    void updateStock(Item dbItem);
//    void deleteAll();
//}
