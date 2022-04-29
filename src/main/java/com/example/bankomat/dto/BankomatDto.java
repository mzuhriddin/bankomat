package com.example.bankomat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BankomatDto {
    @NotBlank
    private String address;
    @NotNull
    private Integer employee;
    @NotNull
    private BigDecimal maxGive, minNotificationValue;
    @NotNull
    private int  percent;
    @NotNull
    @Size(min = 1)
    private Map<String, Integer> cash;
}


