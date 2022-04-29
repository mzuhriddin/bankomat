package com.example.bankomat.repository;

import com.example.bankomat.entity.Bankomat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankomatRepository extends JpaRepository<Bankomat, Integer> {
    List<Bankomat> findAllByActiveTrue();
}
