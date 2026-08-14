//package com.minishop.repository.mybatis;
//
//import com.minishop.domain.Items;
//import com.minishop.domain.item.Item;
//import com.minishop.repository.ItemRepository;
//import com.minishop.repository.mybatis.mapper.ItemMapper;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//@RequiredArgsConstructor
//public class ItemRepositoryMyBatis implements ItemRepository {
//
//    private final ItemMapper itemMapper;
//
//    @Override
//    public Item save(Item item) {
//        itemMapper.saveItem(item);
//        return item;
//    }
//
//    @Override
//    public int update(Long id, Item items) {
//        return itemMapper.updateItem(id,items);
//    }
//
//    @Override
//    public int delete(Long id) {
//        return itemMapper.deleteItem(id);
//    }
//
//    @Override
//    public Optional<Item> findById(Long id) {
//        return Optional.ofNullable(itemMapper.findById(id));
//
//    }
//
//    @Override
//    public List<Item> findAll() {
//        return itemMapper.findAll();
//    }
//
//    @Override
////    public Item findByName(String itemName) {
//        return itemMapper.findByName(itemName);
//    }
//
//    @Override
//    public void update(Item dbItem) {
//        itemMapper.update(dbItem);
//    }
//
//    @Override
//    public void updateStock(Item dbItem) {
//        itemMapper.updateStock(dbItem);
//    }
//
//    @Override
//    public void deleteAll() {
//        itemMapper.deleteAll();
//    }
//
//
//}
