package com.example.bankomat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CardLogin {
    @NotNull(message = "raqam kiritilishi shart")
    private Long number;
    @NotBlank(message = "parol kiritilishi shart")
    @Size(min = 4, max = 4)
    private String password;

    @NotNull
    private Integer bankomat;
}
