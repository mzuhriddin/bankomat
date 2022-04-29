package com.example.bankomat.service;

import com.example.bankomat.dto.ApiResponse;
import com.example.bankomat.dto.BankomatDto;
import com.example.bankomat.dto.BankomatEditDto;
import com.example.bankomat.entity.Bankomat;
import com.example.bankomat.entity.Money;
import com.example.bankomat.entity.MoneyCount;
import com.example.bankomat.entity.User;
import com.example.bankomat.repository.BankomatRepository;
import com.example.bankomat.repository.MoneyCountRepository;
import com.example.bankomat.repository.MoneyRepository;
import com.example.bankomat.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
public record BankomatService(BankomatRepository bankomatRepository, MoneyRepository moneyRepository,
                              MoneyCountRepository moneyCountRepository,
                              UserRepository userRepository) {

    public ApiResponse add(BankomatDto bankomatDto) {
        Optional<User> optionalUser = userRepository.findById(bankomatDto.getEmployee());
        if (optionalUser.isEmpty()) {
            return ApiResponse.builder().success(false).message("Employee not found").build();
        }
        double total = 0;
        ArrayList<MoneyCount> moneyCounts = new ArrayList<>();
        for (String money : bankomatDto.getCash().keySet()) {
            Optional<Money> optionalMoney = moneyRepository.findBySerialNameIgnoreCase(money);
            if (optionalMoney.isEmpty()) {
                return ApiResponse.builder().message("Money with name \"" + money + "\" not found").success(false).build();
            }
            total = Double.sum(total, optionalMoney.get().getAmount().doubleValue() * bankomatDto.getCash().get(money));
            moneyCounts.add(MoneyCount.builder()
                    .money(optionalMoney.get())
                    .count(bankomatDto.getCash().get(money))
                    .build());
        }
        bankomatRepository.save(Bankomat.builder()
                .employee(optionalUser.get())
                .address(bankomatDto.getAddress())
                .maxGive(bankomatDto.getMaxGive())
                .percent(bankomatDto.getPercent())
                .minNotificationValue(bankomatDto.getMinNotificationValue())
                .moneyCounts(moneyCounts)
                .balance(BigDecimal.valueOf(total))
                .build());
        return ApiResponse.builder().message("ADDED").success(true).build();
    }

    public ApiResponse edit(Integer id, BankomatEditDto bankomatEditDto) {
        Optional<Bankomat> optionalBankomat = bankomatRepository.findById(id);
        Optional<User> optionalUser = userRepository.findById(bankomatEditDto.getEmployee());
        if (optionalBankomat.isEmpty() || optionalUser.isEmpty()) {
            return ApiResponse.builder().success(false).message("NOT FOUND").build();
        }
        Bankomat bankomat = optionalBankomat.get();
        bankomat.setPercent(bankomatEditDto.getPercent());
        bankomat.setAddress(bankomatEditDto.getAddress());
        bankomat.setEmployee(optionalUser.get());
        bankomat.setMaxGive(bankomatEditDto.getMaxGive());
        bankomat.setMinNotificationValue(bankomatEditDto.getMinNotificationValue());

        bankomatRepository.save(bankomat);
        return ApiResponse.builder().message("EDITED").success(true).build();
    }

    public ApiResponse delete(Integer id) {
        Optional<Bankomat> optionalBankomat = bankomatRepository.findById(id);
        if (optionalBankomat.isPresent()) {
            Bankomat bankomat = optionalBankomat.get();
            bankomat.setActive(false);
            bankomatRepository.save(bankomat);
            return ApiResponse.builder().success(true).message("DELETED").build();
        }
        return ApiResponse.builder().success(false).message("NOT FOUND").build();
    }
}
