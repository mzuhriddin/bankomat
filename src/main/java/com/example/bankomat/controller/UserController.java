package com.example.bankomat.controller;

import com.example.bankomat.dto.ApiResponse;
import com.example.bankomat.dto.UserDto;
import com.example.bankomat.dto.UserEditDto;
import com.example.bankomat.entity.User;
import com.example.bankomat.repository.UserRepository;
import com.example.bankomat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/user")
@PreAuthorize("hasAuthority('EMPLOYEE_CRUD')")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    @PreAuthorize("hasAuthority('READ_ALL_EMPLOYEE')")
    @GetMapping
    public ResponseEntity getAll() {
        return ResponseEntity.ok().body(userRepository.findAll());
    }

    @PreAuthorize("hasAuthority('READ_ONE_EMPLOYEE')")
    @GetMapping("/{id}")
    public ResponseEntity getOne(@PathVariable Integer id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(optionalUser.get());
    }

    @PreAuthorize("hasAuthority('ADD_EMPLOYEE')")
    @PostMapping
    public ResponseEntity add(@RequestBody UserDto userDto) {
        ApiResponse response = userService.add(userDto);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PreAuthorize("hasAuthority('EDIT_EMPLOYEE')")
    @PutMapping("/{id}")
    public ResponseEntity edit(@PathVariable Integer id, @RequestBody UserEditDto userEditDto, @AuthenticationPrincipal User user) {
        ApiResponse response = userService.edit(id, userEditDto, user);
        return ResponseEntity.status(response.isSuccess() ? 200 : 400).body(response);
    }

    @PreAuthorize("hasAuthority('DELETE_EMPLOYEE')")
    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable Integer id, @AuthenticationPrincipal User user) {
        if (user.getId().equals(id)) {
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .message("You can't delete yourself")
                    .build());
        }
        ApiResponse apiResponse = userService.delete(id);
        return ResponseEntity.status(apiResponse.isSuccess() ? 200 : 404).body(apiResponse);
    }
}
