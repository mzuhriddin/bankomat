package com.example.bankomat.entity;

import com.example.bankomat.entity.enums.CardType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Card {
    @Id
    @Column(length = 16, nullable = false, updatable = false, unique = true)
    private Long number;

    @ManyToOne(cascade = CascadeType.ALL)
    private Bank bank;

    @Column(length = 3, nullable = false)
    private Short cvv;

    @Column(nullable = false)
    private String firstName, lastName;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "date")
    private LocalDate issued = LocalDate.now();

    @Builder.Default
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate expireDate = LocalDate.now().plusYears(4);

    @JsonIgnore
    @Column(length = 4, nullable = false)
    private String password = "0000";

    @Enumerated(value = EnumType.STRING)
    private CardType cardType;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
