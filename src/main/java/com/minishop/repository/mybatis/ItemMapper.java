package com.minishop.repository.mybatis;

import com.minishop.domain.Items;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemMapper {

    Items findById(Long id); //READ
    List<Items> findAll(); //READ
    void saveItem(Items item); //CREATE
    int updateItem(@Param("id") Long id,@Param("item") Items item); //UPDATE
    int deleteItem(Long id); //DELETE
}
