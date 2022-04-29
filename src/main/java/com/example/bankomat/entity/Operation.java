package com.example.bankomat.entity;

import com.example.bankomat.entity.enums.OperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Operation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Bankomat bankomat;

    @Builder.Default
    private LocalDateTime time= LocalDateTime.now();

    @Column(nullable = false, precision = 30)
    private BigDecimal amount;
    @ManyToOne(fetch = FetchType.EAGER)
    private Card card;

    @Enumerated(EnumType.STRING)
    private OperationType operationType;

    @ManyToMany
    private List<MoneyCount> moneyCounts;
}
