package com.wallet.auth.application.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.infrastructure.adapters.in.web.dto.UserDto;
import com.wallet.auth.domain.model.excecption.UserAlreadyExistsException;
import com.wallet.auth.domain.ports.in.AuthenticateUserUseCase;
import com.wallet.auth.domain.ports.in.CreateUserUseCase;
import com.wallet.auth.domain.ports.in.TokenProvider;
import com.wallet.auth.domain.ports.out.PasswordEncoderPort;
import com.wallet.auth.domain.ports.out.UserRepositoryPort;
import com.wallet.auth.infrastructure.adapters.in.web.dto.UserMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService implements CreateUserUseCase, AuthenticateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProvider tokenProvider;

    public UserService(UserRepositoryPort userRepository, PasswordEncoderPort passwordEncoder, TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public UserDto createUser(User user) {
        if (userRepository.existsByEmail(user.email())) {
            log.error("Un usuario con el email {} ya existe.", user.email());
            throw new UserAlreadyExistsException("El usuario con este email ya existe");
        }
        
        String encodedPassword = passwordEncoder.encode(user.password());
        User userWithEncryptedPassword = User.builder()
                .email(user.email())
                .name(user.name())
                .password(encodedPassword)
                .createdAt(user.createdAt())
                .build();
        
        return UserMapper.toDto(userRepository.save(userWithEncryptedPassword));
    }

    @Override
    public Optional<String> authenticate(String email, String rawPassword) {
        User response = userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(rawPassword, user.password()))
                .orElseThrow(() -> {
                    log.error("Intento de autenticación fallido para el email: {}", email);
                    return new IllegalArgumentException("Credenciales inválidas");
                });
        return Optional.of(tokenProvider.generateToken(response.email()));
    }
}