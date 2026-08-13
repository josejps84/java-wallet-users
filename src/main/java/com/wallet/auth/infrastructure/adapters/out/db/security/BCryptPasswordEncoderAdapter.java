package com.wallet.auth.infrastructure.adapters.out.db.security;

import com.wallet.auth.domain.ports.out.PasswordEncoderPort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        System.out.println("Matching raw password with encoded password "+rawPassword+" encoded "+encodedPassword);
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}