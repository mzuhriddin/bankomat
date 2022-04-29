package com.example.bankomat.service;

import com.example.bankomat.entity.User;
import com.example.bankomat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> byUsername = userRepository.findByEmail(email);
        return byUsername.orElseThrow(() -> new UsernameNotFoundException("User topilmadi!"));

    }
}
