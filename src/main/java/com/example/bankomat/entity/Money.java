package com.example.bankomat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Money {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Builder.Default
    private boolean active = true;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String serialName;

    @Column(nullable = false)
    private BigDecimal amount;
}
