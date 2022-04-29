package com.example.bankomat.repository;

import com.example.bankomat.entity.Money;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MoneyRepository extends JpaRepository<Money, Integer> {
    Optional<Money> findBySerialNameIgnoreCase(String string);
}
