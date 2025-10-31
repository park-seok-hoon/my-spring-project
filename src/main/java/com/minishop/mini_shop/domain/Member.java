package com.minishop.mini_shop.domain;

import lombok.Data;

@Data
public class Member {

    private Long id;

    private String name;
    private String email;
    private String password;


}
