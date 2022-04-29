package com.example.bankomat.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EmailDto {
    final String to, message, title;
}
