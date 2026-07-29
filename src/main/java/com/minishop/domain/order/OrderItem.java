package com.minishop.domain.order;

import com.minishop.domain.item.Item;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class OrderItem {

    @GeneratedValue @Id
    @Column(name = "order_item_id")
    private Long id;       // PK


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id")
    private Order order;

    private int quantity;  // 수량

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item; //주문한 상품 정보 자세히 N:1


}
