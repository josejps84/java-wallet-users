package com.wallet.auth.application.services;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.model.exception.UserAlreadyExistsException;
import com.wallet.auth.domain.ports.in.AuthenticateUserUseCase;
import com.wallet.auth.domain.ports.in.CreateUserUseCase;
import com.wallet.auth.domain.ports.in.TokenProvider;
import com.wallet.auth.domain.ports.out.AccountRegistrationPort;
import com.wallet.auth.domain.ports.out.PasswordEncoderPort;
import com.wallet.auth.domain.ports.out.UserRepositoryPort;
import com.wallet.auth.infrastructure.adapters.in.web.dto.UserDto;
import com.wallet.auth.infrastructure.adapters.in.web.dto.UserMapper;

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
    public UserDto createUser(User user) {
        if (userRepository.existsByEmail(user.email())) {
            log.error("Un usuario con el email {} ya existe.", user.email());
            throw new UserAlreadyExistsException("El usuario con este email ya existe");
        }

        String encodedPassword = passwordEncoder.encode(user.password());
        User userWithEncryptedPassword = User.builder()
                .userId(user.userId())
                .identification(user.identification())
                .name(user.name())
                .email(user.email())
                .password(encodedPassword)
                .authType(user.authType())
                .status(user.status())
                .userType(user.userType())
                .registeredAt(user.registeredAt())
                .build();

        User savedUser = userRepository.save(userWithEncryptedPassword);
        return UserMapper.toDto(savedUser);
//
//         try {
//             accountRegistrationPort.register(savedUser.email());
//             return UserMapper.toDto(savedUser);
//         } catch (AccountRegistrationFailedException ex) {
//             userRepository.deleteByEmail(savedUser.email());
//             log.error("Rollback del usuario {} tras fallo en la creación de la cuenta externa.", savedUser.email(), ex);
//             throw ex;
//         }
    }

    @Override
    public Optional<String> authenticate(String email, String rawPassword) {
        log.error("user found "+userRepository.findByEmail(email));
        User response = userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(rawPassword, user.password()))
                .orElseThrow(() -> {
                    log.error("Intento de autenticación fallido para el email: {}", email);
                    return new IllegalArgumentException("Credenciales inválidas");
                });
        return Optional.of(tokenProvider.generateToken(response.email(), response.userType().toString(), response.userId()));
    }
}
