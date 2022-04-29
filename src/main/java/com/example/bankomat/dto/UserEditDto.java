package com.example.bankomat.dto;

import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class UserEditDto {
    private String password;
    @Email
    private String email;
    private String username;
    private Integer bank;
}
