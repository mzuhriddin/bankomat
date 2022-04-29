package com.example.bankomat.service;

import com.example.bankomat.dto.ApiResponse;
import com.example.bankomat.dto.MoneyDto;
import com.example.bankomat.entity.Money;
import com.example.bankomat.repository.MoneyRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;

@Service
public record MoneyService(MoneyRepository moneyRepository) {

    public ApiResponse add(MoneyDto moneyDto) {
        String serial_number=moneyDto.getName().toLowerCase(Locale.ROOT)
                .replaceAll("\\s", "_");
        Optional<Money> bySerialNameIgnoreCase = moneyRepository.findBySerialNameIgnoreCase(serial_number);
        if(bySerialNameIgnoreCase.isPresent()){
            return ApiResponse.builder().message("NOT VALID NAME").build();
        }
        moneyRepository.save(Money.builder()
                .name(moneyDto.getName())
                .serialName(serial_number)
                .amount(BigDecimal.valueOf(moneyDto.getAmount()))
                .build());
        return ApiResponse.builder()
                .message("Money created")
                .success(true)
                .build();
    }

    public ApiResponse delete(Integer id) {
        Optional<Money> optionalMoney = moneyRepository.findById(id);
        if(optionalMoney.isEmpty()){
            return ApiResponse.builder().message("NOT FOUND").build();
        }
        optionalMoney.get().setActive(false);
        return ApiResponse.builder()
                .success(true)
                .message("DELETED")
                .build();
    }
}
