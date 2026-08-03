package com.wallet.auth.application.services;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.ports.in.AuthenticateUserUseCase;
import com.wallet.auth.domain.ports.in.CreateUserUseCase;
import com.wallet.auth.domain.ports.out.PasswordEncoderPort;
import com.wallet.auth.domain.ports.out.UserRepositoryPort;

public class UserService implements CreateUserUseCase, AuthenticateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public UserService(UserRepositoryPort userRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
    public boolean authenticate(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .map(user -> passwordEncoder.matches(rawPassword, user.password()))
                .orElse(false);
    }
}