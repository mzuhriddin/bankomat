package com.example.bankomat.service;

import com.example.bankomat.config.EmailConfig;
import com.example.bankomat.dto.ApiResponse;
import com.example.bankomat.dto.EmailDto;
import com.example.bankomat.dto.UserDto;
import com.example.bankomat.dto.UserEditDto;
import com.example.bankomat.entity.Bank;
import com.example.bankomat.entity.Role;
import com.example.bankomat.entity.User;
import com.example.bankomat.repository.BankRepository;
import com.example.bankomat.repository.RoleRepository;
import com.example.bankomat.repository.UserRepository;
import com.example.bankomat.security.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.Objects;
import java.util.Optional;

@Service
public record UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                          RoleRepository roleRepository, BankRepository bankRepository, JwtProvider jwtProvider,
                          EmailConfig emailConfig) {

    @Value("${company.domain}")
    static String domain;

    public ApiResponse add(UserDto userDto) {
        Optional<Role> optionalRole = roleRepository.findByRoleName(userDto.getRole());
        if (optionalRole.isEmpty()) {
            return ApiResponse.builder()
                    .message("Role NOT FOUND")
                    .success(false)
                    .build();
        }
        Optional<Bank> optionalBank = bankRepository.findById(userDto.getBank());
        if (optionalBank.isEmpty()) {
            return ApiResponse.builder()
                    .message("BANK NOT FOUND")
                    .success(false)
                    .build();
        }
        User save = userRepository.save(User.builder()
                .email(userDto.getEmail())
                .password(passwordEncoder.encode("1234"))
                .role(optionalRole.get())
                .username(userDto.getUsername())
                .enabled(false)
                .build());
        String verification_url = domain + "api/auth/verify/email?token=" +
                jwtProvider.generateToken(save.getEmail(), 2592000L);
        boolean sendEmailHtml = emailConfig.sendEmailHtml(EmailDto.builder()
                .message(verifyEmailHtml(userDto, verification_url))
                .title("Verify email")
                .to(userDto.getEmail())
                .build());
        if (!sendEmailHtml) {
            return ApiResponse.builder()
                    .message("ERROR WITH EMAIL SENDING")
                    .build();
        }

        return ApiResponse.builder()
                .success(true)
                .message("SAVED!")
                .build();

    }
    public String verifyEmailHtml(@Valid UserDto userDto, String url) {
        return "<h1>Hello " + userDto.getUsername() + "!</h1><br/>" +
                "Bank give this email for verification employee.<br/>" +
                "If you are not working in this company ignore this message<br/><br/><br/><br/>" +
                "<table style=\"border-collapse:collapse;border-spacing:0;margin-top:17px\"><tbody><tr><td style=\"background-color:#5b50d6;border:1px none #dadada;border-radius:3px;font-family:Helvetica,Arial,sans-serif;font-size:16px;margin:0;padding:12px 35px;text-align:left;vertical-align:top\" align=\"left\" bgcolor=\"#5B50D6\" valign=\"top\"><a href=\"" +
                url + "\" style=\"background-color:#5b50d6;border:none;border-radius:3px;color:white;display:inline-block;font-size:14px;font-weight:bold;outline:none!important;padding:0px;text-decoration:none\" target=\"_blank\" >Verify</a></td></tr></tbody></table>";
    }
    public ApiResponse edit(Integer id, UserEditDto userEditDto, User auth) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ApiResponse.builder()
                    .message("USER NOT FOUND")
                    .build();
        }
        User user = optionalUser.get();
        if (userEditDto.getEmail() != null) {
            user.setEmail(userEditDto.getEmail());
        }
        if (userEditDto.getPassword() != null && Objects.equals(auth.getId(), id)) {
            BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            user.setPassword(bCryptPasswordEncoder.encode(userEditDto.getPassword()));
        }
        if (userEditDto.getUsername() != null) {
            user.setUsername(userEditDto.getUsername());
        }
        if (userEditDto.getBank() != null) {
            Optional<Bank> optionalBank = bankRepository.findById(userEditDto.getBank());
            if(optionalBank.isEmpty()){
                return ApiResponse.builder().message("BANK NOT FOUND").build();
            }
            user.setBank(optionalBank.get());
        }
        userRepository.save(user);

        return ApiResponse.builder()
                .success(true)
                .message("EDITED!")
                .build();
    }

    public ApiResponse verifyEmail(Integer id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ApiResponse.builder()
                    .message("USER NOT FOUND")
                    .build();
        }
        User user = optionalUser.get();
        user.setEnabled(true);
        userRepository.save(user);
        return ApiResponse.builder()
                .success(true)
                .message("VERIFIED")
                .build();
    }

    public ApiResponse delete(Integer id) {
        if(!userRepository.existsById(id)){
            return ApiResponse.builder()
                    .message("USER NOT FOUND")
                    .build();
        }
        userRepository.deleteById(id);
        return ApiResponse.builder()
                .success(true)
                .message("DELETED!")
                .build();
    }
}
