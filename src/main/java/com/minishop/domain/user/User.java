package com.minishop.domain.user;

import com.minishop.domain.order.Order;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id")
    private Long id;


    @OneToMany(mappedBy = "user")
    List<Order> orders = new ArrayList<>();

    @Embedded
    private UserInfo userInfo;


}