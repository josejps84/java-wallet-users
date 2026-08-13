package com.wallet.auth.application.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.ports.in.AuthenticateUserUseCase;
import com.wallet.auth.domain.ports.in.CreateUserUseCase;
import com.wallet.auth.domain.ports.out.PasswordEncoderPort;
import com.wallet.auth.domain.ports.out.UserRepositoryPort;
import com.wallet.auth.infrastructure.security.JwtTokenProvider;

@Service
public class UserService implements CreateUserUseCase, AuthenticateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepositoryPort userRepository, PasswordEncoderPort passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.email())) {
            throw new IllegalArgumentException("El usuario con este email ya existe");
        }
        
        String encodedPassword = passwordEncoder.encode(user.password());
        User userWithEncryptedPassword = new User(user.email(), user.name(), encodedPassword);
        
        return userRepository.save(userWithEncryptedPassword);
    }

    @Override
    public Optional<String> authenticate(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(rawPassword, user.password()))
                .map(user -> jwtTokenProvider.generateToken(user.email()));
    }
}