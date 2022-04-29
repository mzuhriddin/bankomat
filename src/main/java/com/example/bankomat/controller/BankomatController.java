package com.example.bankomat.controller;

import com.example.bankomat.dto.ApiResponse;
import com.example.bankomat.dto.BankomatDto;
import com.example.bankomat.dto.BankomatEditDto;
import com.example.bankomat.entity.Bankomat;
import com.example.bankomat.repository.BankomatRepository;
import com.example.bankomat.service.BankomatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/bankomat")
@PreAuthorize("hasAuthority('BANKOMATOMAT_CRUD')")
@RequiredArgsConstructor
public class BankomatController {
    private final BankomatRepository bankomatRepository;
    private final BankomatService bankomatService;

    @PreAuthorize("hasAuthority('READ_ALL_BANKOMAT')")
    @GetMapping
    public ResponseEntity getAll() {
        return ResponseEntity.ok().body(bankomatRepository.findAllByActiveTrue());
    }

    @PreAuthorize("hasAuthority('READ_ONE_BANKOMAT')")
    @GetMapping("/{id}")
    public ResponseEntity getOne(@PathVariable Integer id) {
        Optional<Bankomat> optionalBankomat = bankomatRepository.findById(id);
        if (optionalBankomat.isPresent()) {
            return ResponseEntity.ok().body(optionalBankomat.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAuthority('ADD_BANKOMAT')")
    @PostMapping
    public ResponseEntity add(@RequestBody BankomatDto bankomatDto) {
        ApiResponse response = bankomatService.add(bankomatDto);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PreAuthorize("hasAuthority('EDIT_BANKOMAT')")
    @PutMapping("/{id}")
    public ResponseEntity edit(@PathVariable Integer id, @RequestBody BankomatEditDto bankomatEditDto) {
        ApiResponse response = bankomatService.edit(id, bankomatEditDto);
        return ResponseEntity.status(response.isSuccess() ? 200 : 404).body(response);
    }

    @PreAuthorize("hasAuthority('DELETE_BANKOMAT')")
    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable Integer id) {
        ApiResponse delete = bankomatService.delete(id);
        return ResponseEntity.status(delete.isSuccess() ? 200 : 400).body(delete);
    }

    @PreAuthorize("hasAuthority('MONEY_COUNT')")
    @GetMapping("/{id}/money")
    public ResponseEntity getMoneyCount(@PathVariable Integer id) {
        Optional<Bankomat> optionalBankomat = bankomatRepository.findById(id);
        if (optionalBankomat.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(optionalBankomat.get().getMoneyCounts());
    }
}