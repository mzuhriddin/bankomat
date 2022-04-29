package com.example.bankomat.service;

import com.example.bankomat.config.EmailConfig;
import com.example.bankomat.dto.*;
import com.example.bankomat.entity.*;
import com.example.bankomat.entity.enums.OperationType;
import com.example.bankomat.repository.*;
import com.example.bankomat.security.JwtProvider;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OperationService {
    private final OperationRepository operationRepository;
    private final BankomatRepository bankomatRepository;
    private final JwtProvider jwtProvider;
    private final Gson gson;
    private final MoneyRepository moneyRepository;
    private final CardRepository cardRepository;
    private final MoneyCountRepository moneyCountRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailConfig emailConfig;

    Map<LocalDateTime, Long> tries = new LinkedHashMap<>();

    @Value("${company.domain}")
    String domain;

    public ApiResponse input(Integer id, OperationDto operationDto, HttpServletRequest request) {
        Optional<Bankomat> optionalBankomat = bankomatRepository.findById(id);
        if (optionalBankomat.isEmpty()) {
            return ApiResponse.builder()
                    .message("Bankomat not found")
                    .success(false)
                    .build();
        }
        Bankomat bankomat = optionalBankomat.get();
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.split(" ").length < 2) {
            return ApiResponse.builder()
                    .message("Auth failed")
                    .success(false)
                    .build();
        }
        String token = authorization.split(" ")[1];
        if (!jwtProvider.validateToken(token)) {
            return ApiResponse.builder()
                    .message("Token failed")
                    .success(false)
                    .build();
        }
        CardLogin tokenObj = gson.fromJson(jwtProvider.getUsernameFromToken(token), CardLogin.class);
        if (!id.equals(tokenObj.getBankomat())) {
            return ApiResponse.builder()
                    .message("Wrong token info")
                    .build();
        }
        double total = 0;
        Map<MoneyCount, Integer> moneyCounts = new LinkedHashMap<>();
        for (String money : operationDto.getCash().keySet()) {
            Optional<Money> optionalMoney = moneyRepository.findBySerialNameIgnoreCase(money);
            if (optionalMoney.isEmpty()) {
                return ApiResponse.builder().message("Money with name \"" + money + "\" not found").build();
            }
            List<MoneyCount> filteredMoney = bankomat.getMoneyCounts().stream()
                    .filter(moneyCount -> moneyCount.getMoney().getId().equals(optionalMoney.get().getId())).toList();
            if (filteredMoney.size() == 0) {
                return ApiResponse.builder().message("Bankomat have not any cell for money " + money).build();
            }
            moneyCounts.put(filteredMoney.get(0), operationDto.getCash().get(money));
            total += optionalMoney.get().getAmount().doubleValue() * operationDto.getCash().get(money);
        }
        if (total >= bankomat.getMaxGive().doubleValue()) {
            return ApiResponse.builder()
                    .message("Max transaction value is " + bankomat.getMaxGive())
                    .build();
        }
        ApiResponse cardStatus = checkCard(tokenObj.getNumber());
        if (!cardStatus.isSuccess()) {
            return ApiResponse.builder().message(cardStatus.getMessage()).build();
        }
        Card card = (Card) cardStatus.getObject();
        card.setBalance(card.getBalance().add(BigDecimal.valueOf(total - commission(bankomat, card, total))));
        cardRepository.save(card);
        for (MoneyCount moneyCount : moneyCounts.keySet()) {
            moneyCount.setCount(moneyCount.getCount() + moneyCounts.get(moneyCount));
            moneyCountRepository.save(moneyCount);
        }
        bankomat.setBalance(bankomat.getBalance().add(BigDecimal.valueOf(total)));
        bankomatRepository.save(bankomat);
        Operation save = operationRepository.save(Operation.builder()
                .amount(BigDecimal.valueOf(total))
                .bankomat(bankomat)
                .card(card)
                .moneyCounts(moneyCounts.keySet().stream().toList())
                .operationType(OperationType.INPUT)
                .build());


        return ApiResponse.builder()
                .success(true)
                .message("Operation success!")
                .object(save)
                .build();
    }

    public ApiResponse output(Integer id, OperationOutputDto operationDto, HttpServletRequest request) {
        Optional<Bankomat> optionalBankomat = bankomatRepository.findById(id);
        if (optionalBankomat.isEmpty()) {
            return ApiResponse.builder()
                    .message("Bankomat not found")
                    .build();
        }
        Bankomat bankomat = optionalBankomat.get();
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.split(" ").length < 2) {
            return ApiResponse.builder()
                    .message("Auth failed")
                    .build();
        }
        String token = authorization.split(" ")[1];
        if (!jwtProvider.validateToken(token)) {
            return ApiResponse.builder()
                    .message("Token failed")
                    .build();
        }
        CardLogin tokenObj = gson.fromJson(jwtProvider.getUsernameFromToken(token), CardLogin.class);
        if (!id.equals(tokenObj.getBankomat())) {
            return ApiResponse.builder()
                    .message("Wrong token info")
                    .build();
        }
        if (operationDto.getAmount() >= bankomat.getMaxGive().doubleValue()) {
            return ApiResponse.builder()
                    .message("Max transaction value is " + bankomat.getMaxGive())
                    .build();
        }
        if (operationDto.getAmount() > bankomat.getBalance().doubleValue()) {
            return ApiResponse.builder()
                    .message("Bankomatda buncha pul yoq")
                    .build();
        }
        ApiResponse cardStatus = checkCard(tokenObj.getNumber());
        if (!cardStatus.isSuccess()) {
            return ApiResponse.builder().message(cardStatus.getMessage()).build();
        }
        Card card = (Card) cardStatus.getObject();
        if (card.getBalance().doubleValue() <= operationDto.getAmount()) {
            return ApiResponse.builder()
                    .message("Amount greater than card amount")
                    .build();
        }
        Map<MoneyCount, Integer> moneyCounts = new LinkedHashMap<>();
        bankomat.getMoneyCounts().sort((o1, o2) ->
                o1.getMoney().getAmount().doubleValue() > o2.getMoney().getAmount().doubleValue() ? 0 : 1);
        double left = operationDto.getAmount();
        for (MoneyCount moneyCount : bankomat.getMoneyCounts()) {
            if (left == 0) break;
            double value = moneyCount.getMoney().getAmount().doubleValue();
            if (value > left) {
                continue;
            }
            int count;
            if (value == left) {
                if (moneyCount.getCount() == 0) {
                    continue;
                }
                count = 1;
            } else {
                if (left / value <= moneyCount.getCount()) {
                    count = (int) (left / value);
                } else {
                    continue;
                }
            }
            moneyCount.setCount(moneyCount.getCount() - count);
            moneyCounts.put(moneyCount, count);
            left -= moneyCount.getMoney().getAmount().multiply(BigDecimal.valueOf(count)).doubleValue();
        }
        if (left != 0) {
            return ApiResponse.builder().message("Bankomatda kerakli pullar yoq").build();
        }
        card.setBalance(BigDecimal.valueOf(card.getBalance().doubleValue() - operationDto.getAmount()
                - commission(bankomat, card, operationDto.getAmount())));
        cardRepository.save(card);
        bankomat.setBalance(BigDecimal.valueOf(bankomat.getBalance().doubleValue() - operationDto.getAmount()));
        bankomatRepository.save(bankomat);
        if (bankomat.getBalance().doubleValue() <= bankomat.getMinNotificationValue().doubleValue()) {
            Thread thread = new Thread(() -> sendBankomatAmountMinNotification(bankomat));
            thread.start();
        }
        Map<String, Integer> moneyCountDto = new LinkedHashMap<>();
        for (MoneyCount moneyCount : moneyCounts.keySet()) {
            moneyCountDto.put(moneyCount.getMoney().getSerialName(), moneyCounts.get(moneyCount));
        }
        operationRepository.save(Operation.builder()
                .amount(BigDecimal.valueOf(operationDto.getAmount()))
                .bankomat(bankomat)
                .card(card)
                .moneyCounts(moneyCounts.keySet().stream().toList())
                .operationType(OperationType.OUTPUT)
                .build());

        return ApiResponse.builder()
                .success(true)
                .message("Operation success!")
                .object(moneyCountDto)
                .build();
    }

    public ApiResponse check(CardLogin cardLogin) {
        Optional<Bankomat> optionalBankomat = bankomatRepository.findById(cardLogin.getBankomat());
        if (optionalBankomat.isEmpty()) {
            return ApiResponse.builder()
                    .message("Bankomat not found")
                    .build();
        }
        ApiResponse apiResponse = checkCard(cardLogin.getNumber());
        if (!apiResponse.isSuccess()) {
            return ApiResponse.builder().message(apiResponse.getMessage()).build();
        }
        Card card = (Card) apiResponse.getObject();
        if (!passwordEncoder.matches(cardLogin.getPassword(), card.getPassword())) {
            int count = 0;
            for (LocalDateTime localDateTime : tries.keySet().stream().filter(localDateTime ->
                    tries.get(localDateTime).equals(card.getNumber())).toList()) {
                if (localDateTime.isBefore(LocalDateTime.now().minusHours(1))) {
                    continue;
                }
                count++;
            }
            if (count >= 2) {
                card.setActive(false);
                cardRepository.save(card);
                tries.values().remove(card.getNumber());
                return ApiResponse.builder()
                        .message("Card blocked")
                        .build();
            }
            tries.put(LocalDateTime.now(), card.getNumber());
            return ApiResponse.builder()
                    .message("Invalid password")
                    .build();
        }
        String token = jwtProvider.generateToken(gson.toJson(cardLogin), 1800000L);
        return ApiResponse.builder()
                .success(true)
                .message("Success")
                .object(token)
                .build();
    }

    public ApiResponse fillUp(Integer id, OperationDto operationDto, User user) {
        Optional<Bankomat> bankomatOptional = bankomatRepository.findById(id);
        if (bankomatOptional.isEmpty()) {
            return ApiResponse.builder().message("Bankomat not found").build();
        }
        Bankomat bankomat = bankomatOptional.get();
        if (!bankomat.getEmployee().getId().equals(user.getId())) {
            return ApiResponse.builder().message("Bankomatga mas'ul odam emassiz!").build();
        }
        Map<MoneyCount, Integer> moneyCounts = new LinkedHashMap<>();
        double total = 0;
        for (String money : operationDto.getCash().keySet()) {
            Optional<Money> optionalMoney = moneyRepository.findBySerialNameIgnoreCase(money);
            if (optionalMoney.isEmpty()) {
                return ApiResponse.builder().message("Money with name \"" + money + "\" not found").build();
            }
            List<MoneyCount> filteredMoney = bankomat.getMoneyCounts().stream()
                    .filter(moneyCount -> moneyCount.getMoney().getId().equals(optionalMoney.get().getId())).toList();
            if (filteredMoney.size() == 0) {
                return ApiResponse.builder().message("Bankomat have not any cell for money " + money).build();
            }
            MoneyCount moneyCount = filteredMoney.get(0);
            moneyCounts.put(moneyCount, operationDto.getCash().get(money));
            total += optionalMoney.get().getAmount().doubleValue() * operationDto.getCash().get(money);
        }
        for (MoneyCount moneyCount : moneyCounts.keySet()) {
            moneyCount.setCount(moneyCount.getCount() + moneyCounts.get(moneyCount));
            moneyCountRepository.save(moneyCount);
        }
        bankomat.setBalance(bankomat.getBalance().add(BigDecimal.valueOf(total)));
        bankomatRepository.save(bankomat);
        Operation save = operationRepository.save(Operation.builder()
                .amount(BigDecimal.valueOf(total))
                .bankomat(bankomat)
                .moneyCounts(moneyCounts.keySet().stream().toList())
                .operationType(OperationType.FILL_UP)
                .build());

        return ApiResponse.builder()
                .success(true)
                .object(save)
                .message("Success replenish!")
                .build();
    }

    public void sendBankomatAmountMinNotification(Bankomat bankomat) {
        User responsible = bankomat.getEmployee();
        StringBuilder moneyList = new StringBuilder();
        for (MoneyCount moneyCount : bankomat.getMoneyCounts()) {
            moneyList.append("<b>").append(moneyCount.getMoney().getName())
                    .append(" - ").append(moneyCount.getCount())
                    .append(" = ").append(moneyCount.getCount() * moneyCount.getMoney().getAmount().doubleValue())
                    .append("<br/>");
        }
        emailConfig.sendEmailHtml(EmailDto.builder()
                .to(responsible.getEmail())
                .title("Bankomatda pul tugamoqda")
                .message("<h1> Assalomu aleykum " + responsible.getUsername() + "!</h1><br/><br/>" +
                        "<b>" + bankomat.getAddress() + "</b> addresida joylashgan bankomatda <b>"
                        + bankomat.getBalance() + "<b/> miqdorda pul qoldi<br/><br/>" +
                        moneyList + "<br/>" +
                        "<a href=\"" + domain + "api/bankomat/" + bankomat.getId() + "\">Batafsil</a>")

                .build());
    }

    public ApiResponse checkCard(Long id) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        if (optionalCard.isEmpty()) {
            return ApiResponse.builder().message("Card not found").build();
        }
        Card card = optionalCard.get();
        if (!card.isActive()) {
            return ApiResponse.builder().message("Card blocked").build();
        }
        if (card.getExpireDate().isBefore(LocalDate.now())) {
            return ApiResponse.builder().message("Card expired").build();
        }
        return ApiResponse.builder().success(true).object(optionalCard.get()).build();
    }


    private double commission(Bankomat bankomat, Card card, double amount) {
        return amount / 100 * (bankomat.getEmployee().getBank().getCard() == card.getCardType()
                ? 0 : bankomat.getPercent());
    }
}
