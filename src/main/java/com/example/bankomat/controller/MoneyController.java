package com.example.bankomat.controller;

import com.example.bankomat.dto.ApiResponse;
import com.example.bankomat.dto.MoneyDto;
import com.example.bankomat.repository.MoneyRepository;
import com.example.bankomat.service.MoneyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasAuthority('MONEY_CRUD')")
@RestController
@RequestMapping("/money")
@RequiredArgsConstructor
public class MoneyController {
    private final MoneyRepository moneyRepository;
    private final MoneyService moneyService;

    @GetMapping
    public ResponseEntity getAll() {
        return ResponseEntity.ok().body(moneyRepository.findAll());
    }

    @PreAuthorize("hasAnyAuthority('MONEY_ADD')")
    @PostMapping
    public ResponseEntity add(@RequestBody MoneyDto moneyDto) {
        ApiResponse apiResponse = moneyService.add(moneyDto);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 400).body(apiResponse);
    }

    @PreAuthorize("hasAnyAuthority('MONEY_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable Integer id) {
        ApiResponse apiResponse = moneyService.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 400).body(apiResponse.getMessage());
    }
}
