package com.minishop.repository.mybatis;

import com.minishop.domain.Item;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ItemMapper {
    void save(Item item); //CREATE
    void update(Item item); //UPDATE
    void delete(Long id); //DELETE
    Item findById(Long id); //READ
    List<Item> findAll(); //READ
}
