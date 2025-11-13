package com.minishop.repository.mybatis.mapper;

import com.minishop.domain.Items;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemMapper {

    Items findById(Long id);
    List<Items> findAll(); //READ
    void saveItem(Items item); //CREATE
    int updateItem(@Param("id") Long id,@Param("item") Items item); //UPDATE
    int deleteItem(Long id); //DELETE
    Items findByName(String itemName);  //예외 처리를 위한 아이템 이름 가져오기
    int update(Items dbItem);
}
