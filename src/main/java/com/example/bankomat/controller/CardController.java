package com.example.bankomat.controller;

import com.example.bankomat.dto.ApiResponse;
import com.example.bankomat.dto.CardDto;
import com.example.bankomat.entity.Card;
import com.example.bankomat.entity.User;
import com.example.bankomat.repository.CardRepository;
import com.example.bankomat.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/card")
@PreAuthorize("hasAuthority('CARD_CRUD')")
@RequiredArgsConstructor
public class CardController {
    private final CardRepository cardRepository;
    private final CardService cardService;

    @PreAuthorize("hasAuthority('READ_ALL_CARD')")
    @GetMapping
    public ResponseEntity getAll() {
        return ResponseEntity.ok().body(cardRepository.findAll());
    }

    @PreAuthorize("hasAuthority('READ_ONE_CARD')")
    @GetMapping("/{id}")
    public ResponseEntity getOne(@PathVariable Long id) {
        Optional<Card> optionalCard = cardRepository.findById(id);
        if (optionalCard.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(optionalCard.get());
    }

    @PreAuthorize("hasAuthority('ADD_CARD')")
    @PostMapping
    public ResponseEntity add(@Valid @RequestBody CardDto cardDto, @AuthenticationPrincipal User user) {
        ApiResponse response = cardService.add(cardDto, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/block/{number}")
    @PreAuthorize("hasAnyAuthority('CARD_BLOCK')")
    public ResponseEntity blockCard(@PathVariable Long number) {
        ApiResponse apiResponse = cardService.block(number);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 400).body(apiResponse.getMessage());
    }

    @PostMapping("/unblock/{number}")
    @PreAuthorize("hasAnyAuthority('CARD_UNBLOCK')")
    public ResponseEntity unblockCard(@PathVariable Long number) {
        ApiResponse apiResponse = cardService.unblock(number);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 400).body(apiResponse.getMessage());
    }

    @PreAuthorize("hasAuthority('EDIT_CARD')")
    @PatchMapping("/{id}")
    public ResponseEntity edit(@PathVariable Long id, @RequestParam String password) {
        ApiResponse response = cardService.edit(id, password);
        return ResponseEntity.status(response.isSuccess() ? 200 : 404).body(response);
    }

}
