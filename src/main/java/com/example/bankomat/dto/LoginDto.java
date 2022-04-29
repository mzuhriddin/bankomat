package com.example.bankomat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginDto {
    @Email
    @NotBlank(message = "email kiritilishi shart")
    private String email;
    @NotBlank(message = "password kiritilishi shart")
    private String password;
}
