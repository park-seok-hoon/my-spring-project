package com.minishop.repository.mybatis;

import com.minishop.domain.Items;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ItemMapper {

    Items findById(Long id); //READ
    List<Items> findAll(); //READ
    void save(Items item); //CREATE
    int update(@Param("id") Long id,@Param("item") Items item); //UPDATE
    void delete(Long id); //DELETE
}
