package com.example.bankomat.controller;

import com.example.bankomat.dto.ApiResponse;
import com.example.bankomat.dto.LoginDto;
import com.example.bankomat.entity.User;
import com.example.bankomat.repository.UserRepository;
import com.example.bankomat.security.JwtProvider;
import com.example.bankomat.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody LoginDto loginDto, HttpServletResponse res) {
        Optional<User> byUsername = userRepository.findByEmail(loginDto.getEmail());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (byUsername.isEmpty() || !byUsername.get().isEnabled() || !passwordEncoder.matches(loginDto.getPassword(), byUsername.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid username or password");
        }
        String token = jwtProvider.generateToken(loginDto.getEmail());
        Cookie cookie = new Cookie("token", token);
        res.addCookie(cookie);
        return ResponseEntity.ok(token);
    }

    @SneakyThrows
    @PostMapping("/verify/email")
    public ResponseEntity send(@Param("token") String token) {
        if (jwtProvider.validateToken(token)) {
            Optional<User> optionalUser = userRepository.findByEmail(jwtProvider.getUsernameFromToken(token));
            if (optionalUser.isEmpty() || optionalUser.get().isEnabled()) {
                return ResponseEntity.badRequest().body("token failed");
            }
            ApiResponse apiResponse = userService.verifyEmail(optionalUser.get().getId());
            if (apiResponse.isSuccess()) {
                return ResponseEntity.ok(apiResponse);
            }
        }
        return ResponseEntity.badRequest().body("Failed!");
    }
}
