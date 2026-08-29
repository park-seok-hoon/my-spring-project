package com.minishop.domain.order;

import com.minishop.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="orders")
public class Order {

    @GeneratedValue @Id
    @Column(name = "order_id")
    private Long id;                // 주문 ID   (Orders PK)


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>(); // 주문 상품 목록  1:N 관계  1개의 주문에 여러개의 상품들을 주문 가능

    private LocalDateTime orderDate;// 주문일
    private int totalPrice;         // 총 가격


    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;


}