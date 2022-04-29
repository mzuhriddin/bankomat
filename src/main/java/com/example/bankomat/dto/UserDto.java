package com.example.bankomat.dto;

import com.example.bankomat.entity.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDto {
    @Email(message = "Email required field")
    @NotNull
    private String email;
    @NotNull
    private RoleEnum role;
    @NotNull
    @NotBlank
    private String username;
    private Integer bank;
}
