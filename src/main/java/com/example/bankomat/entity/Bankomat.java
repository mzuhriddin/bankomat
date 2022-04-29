package com.example.bankomat.entity;

import com.example.bankomat.entity.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Bankomat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(value = EnumType.STRING)
    private CardType cardType;

    @Column(nullable = false)
    private BigDecimal maxGive;

    @Column(nullable = false)
    private int percent;

    @Column(nullable = false)
    private BigDecimal minNotificationValue;

    @ManyToOne
    @JoinColumn(nullable = false)
    private User employee;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(columnDefinition = "text", nullable = false)
    private String address;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private List<MoneyCount> moneyCounts;

    @Builder.Default
    private boolean active = true;

}