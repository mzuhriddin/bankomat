package com.example.bankomat.repository;

import com.example.bankomat.entity.MoneyCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoneyCountRepository extends JpaRepository<MoneyCount, Integer> {
}
