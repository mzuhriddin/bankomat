package com.example.bankomat.controller;

import com.example.bankomat.dto.ApiResponse;
import com.example.bankomat.dto.CardLogin;
import com.example.bankomat.dto.OperationDto;
import com.example.bankomat.dto.OperationOutputDto;
import com.example.bankomat.entity.Operation;
import com.example.bankomat.entity.User;
import com.example.bankomat.entity.enums.OperationType;
import com.example.bankomat.repository.BankomatRepository;
import com.example.bankomat.repository.OperationRepository;
import com.example.bankomat.service.OperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/operation")
@RequiredArgsConstructor
public class OperationController {
    private final BankomatRepository bankomatRepository;
    private final OperationRepository operationRepository;
    private final OperationService operationService;

    @PostMapping("/input/{id}")
    public ResponseEntity input(@PathVariable Integer id, @RequestBody OperationDto operationDto, HttpServletRequest request) {
        ApiResponse apiResponse = operationService.input(id, operationDto, request);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 400).body(apiResponse);
    }

    @PostMapping("/output/{id}")
    public ResponseEntity output(@PathVariable Integer id, @RequestBody OperationOutputDto operationDto, HttpServletRequest request) {
        ApiResponse apiResponse = operationService.output(id, operationDto, request);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 400).body(apiResponse);
    }


    @PostMapping("/fillUp/{id}")
    @PreAuthorize("hasAnyAuthority('CARD_CRUD')")
    public ResponseEntity fillUp(@PathVariable Integer id, @RequestBody OperationDto operationDto, @AuthenticationPrincipal User user) {
        ApiResponse apiResponse = operationService.fillUp(id, operationDto, user);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 400).body(apiResponse);
    }

    @PostMapping("/check")
    public ResponseEntity getToken(@Valid @RequestBody CardLogin cardLogin) {
        ApiResponse apiResponse = operationService.check(cardLogin);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 400).body(apiResponse);
    }


    @GetMapping("/output/{id}/today")
    public ResponseEntity todayOutput(@PathVariable Integer id) {
        List<Operation> result = operationRepository.findAllByTimeAfterAndOperationTypeAndBankomat_id(LocalDateTime.now().withHour(0),
                OperationType.OUTPUT, id);
        return ResponseEntity.status(bankomatRepository.existsById(id) ? 200 : 404).body(result);
    }

    @GetMapping("/input/{id}/today")
    public ResponseEntity todayInput(@PathVariable Integer id) {
        List<Operation> result = operationRepository.findAllByTimeAfterAndOperationTypeAndBankomat_id(LocalDateTime.now().withHour(0),
                OperationType.INPUT, id);
        return ResponseEntity.status(bankomatRepository.existsById(id) ? 200 : 404).body(result);
    }

    @PreAuthorize("hasAnyAuthority('FILL_UP_READ')")
    @GetMapping("/fillUp/{id}")
    public ResponseEntity fillUp(@PathVariable Integer id) {
        List<Operation> result = operationRepository.findAllByOperationTypeAndBankomat_id(OperationType.FILL_UP, id);
        return ResponseEntity.status(bankomatRepository.existsById(id) ? 200 : 404).body(result);
    }
}
