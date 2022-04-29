package com.example.bankomat.repository;

import com.example.bankomat.entity.Operation;
import com.example.bankomat.entity.enums.OperationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OperationRepository extends JpaRepository<Operation, Long> {
    List<Operation> findAllByTimeAfterAndOperationTypeAndBankomat_id(LocalDateTime time, OperationType operationType, Integer bankomat_id);

    List<Operation> findAllByOperationTypeAndBankomat_id(OperationType fillUp, Integer id);
}
