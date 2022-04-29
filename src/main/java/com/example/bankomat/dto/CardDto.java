package com.example.bankomat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CardDto {
    @NotBlank
    private String firstName, lastName;
    @Size(max = 4, min = 4)
    private String password;

    private double balance;
}
