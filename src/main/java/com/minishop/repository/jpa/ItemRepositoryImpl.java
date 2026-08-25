package com.minishop.repository.jpa;

import com.minishop.domain.item.Item;
import com.minishop.exception.AppException;
import com.minishop.exception.ErrorCode;
import com.minishop.repository.ItemRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ItemRepositoryImpl implements ItemRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Item save(Item item) {
        em.persist(item);
        return item;
    }

    @Override
    public int delete(Long id) {
        Item item= em.find(Item.class,id);

        if (item == null) {
            throw new AppException(ErrorCode.ITEM_NOT_FOUND);
        }
        em.remove(item);

        return 1; // 삭제 성공
    }

    @Override
    public Optional<Item> findById(Long id) {
        Item item = em.find(Item.class,id);

        return Optional.ofNullable(item);
    }

    @Override
    public List<Item> findAll() {
        return em.createQuery("select i from Item i",Item.class).getResultList();
    }

    @Override
    public Item findByName(String itemName) {
        List<Item> result = em.createQuery("select i from Item i where i.name = :name", Item.class).setParameter("name",itemName).getResultList();
        return result.isEmpty() ? null : result.get(0);
        
    }

    @Override
    public int update(Long id, Item updateItem) {
        Item dbItem = em.find(Item.class,id);
        if(dbItem==null){
            return 0;
        }

        dbItem.setName(updateItem.getName());
        dbItem.setPrice(updateItem.getPrice());
        dbItem.setStockQuantity(updateItem.getStockQuantity());

        return 0;
    }

    @Override
    public void deleteAll() {
        em.createQuery("Delete from Item i").executeUpdate();
    }
}
