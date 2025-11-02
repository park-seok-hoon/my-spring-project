package com.minishop.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    private Long id;
    private String username;
    private String password;
    private String email;
}