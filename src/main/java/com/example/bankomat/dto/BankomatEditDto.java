package com.example.bankomat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class BankomatEditDto {
    @NotBlank
    private String address;
    @NotNull
    private Integer employee;
    @NotNull
    private BigDecimal maxGive, minNotificationValue;
    @NotNull
    private int percent;
}
