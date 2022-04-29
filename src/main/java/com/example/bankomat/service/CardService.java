package com.example.bankomat.service;

import com.example.bankomat.dto.ApiResponse;
import com.example.bankomat.dto.CardDto;
import com.example.bankomat.entity.Bank;
import com.example.bankomat.entity.Card;
import com.example.bankomat.entity.User;
import com.example.bankomat.repository.CardRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public record CardService(CardRepository cardRepository, PasswordEncoder passwordEncoder) {
    public Long generate(Long min, Long max) {
        return Math.round(Math.random() * (max - min) + min);
    }

    public Long generateNumber() {
        Long number = Long.parseLong("8600" + generate(1000L, 9999L) + generate(1000L, 9999L) + generate(1000L, 9999L));
        if (cardRepository.existsById(number)) {
            return generateNumber();
        }
        return number;
    }

    public ApiResponse add(CardDto cardDto, User user) {
        Bank bank = user.getBank();
        Card save = cardRepository.save(Card.builder()
                .firstName(cardDto.getFirstName())
                .number(generateNumber())
                .lastName(cardDto.getLastName())
                .cvv(Short.valueOf("" + Math.round(Math.random() * (999 - 100) + 100)))
                .password(passwordEncoder.encode(String.valueOf(cardDto.getPassword())))
                .cardType(bank.getCard())
                .balance(BigDecimal.valueOf(cardDto.getBalance()))
                .build());
        return ApiResponse.builder().message("ADDED").success(true).object(save).build();
    }

    public ApiResponse edit(Long id, String password) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        if (optionalCard.isEmpty()) {
            return ApiResponse.builder().message("CARD NOT FOUND").success(true).build();
        }
        Card card = optionalCard.get();
        card.setPassword(passwordEncoder.encode(password));
        cardRepository.save(card);
        return ApiResponse.builder().message("EDITED").success(true).build();
    }

    public ApiResponse block(Long number) {
        Optional<Card> optionalCard = cardRepository.findById(number);
        if(optionalCard.isEmpty()){
            return ApiResponse.builder()
                    .success(false)
                    .message("NOT FOUND")
                    .build();
        }
        if(!optionalCard.get().isActive()){
            return ApiResponse.builder()
                    .success(false)
                    .message("CARD ALREADY BLOCKED")
                    .build();
        }
        optionalCard.get().setActive(false);
        cardRepository.save(optionalCard.get());
        return ApiResponse.builder()
                .success(true)
                .message("BLOCKED")
                .build();
    }

    public ApiResponse unblock(Long number) {
        Optional<Card> optionalCard = cardRepository.findById(number);
        if(optionalCard.isEmpty()){
            return ApiResponse.builder()
                    .success(false)
                    .message("NOT FOUND")
                    .build();
        }
        if(optionalCard.get().isActive()){
            return ApiResponse.builder()
                    .success(false)
                    .message("CARD ALREADY ACTIVE")
                    .build();
        }
        optionalCard.get().setActive(true);
        cardRepository.save(optionalCard.get());
        return ApiResponse.builder()
                .success(true)
                .message("Success unblocked!")
                .build();
    }
}
