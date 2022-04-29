package com.example.bankomat.component;

import com.example.bankomat.entity.Bank;
import com.example.bankomat.entity.Money;
import com.example.bankomat.entity.Role;
import com.example.bankomat.entity.User;
import com.example.bankomat.entity.enums.CardType;
import com.example.bankomat.entity.enums.PermissionEnum;
import com.example.bankomat.entity.enums.RoleEnum;
import com.example.bankomat.repository.BankRepository;
import com.example.bankomat.repository.MoneyRepository;
import com.example.bankomat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BankRepository bankRepository;
    private final MoneyRepository moneyRepository;

    @Value("${spring.sql.init.mode}")
    String mode;

    @Override
    public void run(String... args) {
        if (mode.equalsIgnoreCase("always")) {
            PermissionEnum[] permissionEnums = PermissionEnum.values();
            Bank nbu = bankRepository.save(Bank.builder()
                    .address("Toshkent shahar, Mirobod tumani, 8-mart ko'chasi")
                    .card(CardType.UZCARD)
                    .name("NBU")
                    .build());
            userRepository.save(User.builder()
                    .password(passwordEncoder.encode("1234"))
                    .username("director")
                    .email("director@gmail.com")
                    .bank(nbu)
                    .role(Role.builder()
                            .roleName(RoleEnum.ROLE_DIRECTOR)
                            .permissionEnum(Arrays.stream(permissionEnums).toList())
                            .build())
                    .build());
            userRepository.save(User.builder()
                    .role(Role.builder()
                            .roleName(RoleEnum.ROLE_MODERATOR)
                            .permissionEnum(List.of(
                                    PermissionEnum.FILL_UP_ATM,
                                    PermissionEnum.BANKOMAT_CRUD, PermissionEnum.CARD_CRUD,
                                    PermissionEnum.MONEY_CRUD
                            ))
                            .build())
                    .bank(nbu)
                    .email("manager@gmail.com")
                    .username("manager")
                    .password(passwordEncoder.encode("1234"))
                    .build());
            moneyRepository.saveAll(List.of(
                    Money.builder().name("One dollar").serialName("ONE_DOLLAR").amount(BigDecimal.ONE).build(),
                    Money.builder().name("Ten dollar").serialName("TEN_DOLLAR").amount(BigDecimal.TEN).build(),
                    Money.builder().name("Fifty dollar").serialName("FIFTY_DOLLAR").amount(BigDecimal.valueOf(50)).build(),
                    Money.builder().name("Hundred dollar").serialName("HUNDRED_DOLLAR").amount(BigDecimal.valueOf(100)).build(),
                    Money.builder().name("Five dollar").serialName("FIVE_DOLLAR").amount(BigDecimal.valueOf(5)).build()
            ));
        }
    }
}
