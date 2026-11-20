package com.wallet.auth.application.services;

import org.springframework.stereotype.Service;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.model.exception.AccountRegistrationFailedException;
import com.wallet.auth.domain.model.exception.InvalidCredentialsException;
import com.wallet.auth.domain.model.exception.UserAlreadyExistsException;
import com.wallet.auth.domain.ports.in.AuthenticateUserUseCase;
import com.wallet.auth.domain.ports.in.CreateUserUseCase;
import com.wallet.auth.domain.ports.out.AccountRegistrationPort;
import com.wallet.auth.domain.ports.out.PasswordEncoderPort;
import com.wallet.auth.domain.ports.out.TokenProvider;
import com.wallet.auth.domain.ports.out.UserRepositoryPort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserService implements CreateUserUseCase, AuthenticateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TokenProvider tokenProvider;
    private final AccountRegistrationPort accountRegistrationPort;

    public UserService(UserRepositoryPort userRepository, PasswordEncoderPort passwordEncoder,
            TokenProvider tokenProvider, AccountRegistrationPort accountRegistrationPort) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.accountRegistrationPort = accountRegistrationPort;
    }

    @Override
    public User createUser(User user) {
        String email = User.normalizeEmail(user.email());
        if (userRepository.existsByEmail(email)) {
            log.warn("Registration rejected, email already in use: {}", email);
            throw new UserAlreadyExistsException("El usuario con este email ya existe");
        }

        User savedUser = userRepository.save(user.toBuilder()
                .email(email)
                .password(passwordEncoder.encode(user.password()))
                .build());

        try {
            accountRegistrationPort.register(savedUser);
        } catch (AccountRegistrationFailedException ex) {
            // Undo the registration so the user can retry instead of ending up without a wallet
            userRepository.deleteByEmail(savedUser.email());
            log.error("User {} rolled back because the account service failed", savedUser.email(), ex);
            throw ex;
        }
        log.info("User {} registered with id {}", savedUser.email(), savedUser.userId());
        return savedUser;
    }

    @Override
    public String authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(User.normalizeEmail(email))
                .filter(candidate -> passwordEncoder.matches(rawPassword, candidate.password()))
                .orElseThrow(() -> {
                    log.warn("Failed login attempt for {}", email);
                    return new InvalidCredentialsException();
                });
        return tokenProvider.generateToken(user.email(), user.userType().name(), user.userId());
    }
}
