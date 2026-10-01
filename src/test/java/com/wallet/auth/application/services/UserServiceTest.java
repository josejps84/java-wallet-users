package com.wallet.auth.application.services;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.model.UserAuthType;
import com.wallet.auth.domain.model.UserType;
import com.wallet.auth.domain.model.exception.UserAlreadyExistsException;
import com.wallet.auth.domain.ports.in.TokenProvider;
import com.wallet.auth.domain.ports.out.PasswordEncoderPort;
import com.wallet.auth.domain.ports.out.UserRepositoryPort;
import com.wallet.auth.infrastructure.adapters.in.web.dto.UserDto;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock 
    UserRepositoryPort repo;
    @Mock 
    PasswordEncoderPort passEncoder;
    @Mock 
    TokenProvider tokenProvider;

    @InjectMocks 
    UserService service;
    
    @Test
    void testAuthenticate() {
        User user = createTestUser(false);
        User userPassEncoded = encodePass(user);
        String tokenGenerated = "tokenGenerated";
        
        Mockito.when(repo.findByEmail(user.email())).thenReturn(Optional.ofNullable(userPassEncoded));
        Mockito.when(passEncoder.matches(user.password(), userPassEncoded.password())).thenReturn(true);
        Mockito.when(tokenProvider.generateToken(user.email(), user.userType().toString(), user.userId())).thenReturn(tokenGenerated);

        Optional<String> result = service.authenticate(user.email(), "not-a-real-password");

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(tokenGenerated, result.get());
    }
    
    @Test
    void testAuthenticateWrongPassword() {
        User user = createTestUser(false);
        User userPassEncoded = encodePass(user);
        
        Mockito.when(repo.findByEmail(user.email())).thenReturn(Optional.ofNullable(userPassEncoded));
        Mockito.when(passEncoder.matches(user.password(), userPassEncoded.password())).thenReturn(false);

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.authenticate(user.email(), "not-a-real-password");
        });

        Assertions.assertEquals(ex.getMessage(), "Credenciales inválidas");
    }

    @Test
    void testAuthenticateWrongEmail() {
        User user = createTestUser(false);
        
        Mockito.when(repo.findByEmail(user.email())).thenReturn(Optional.ofNullable(null));

        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            service.authenticate(user.email(), "not-a-real-password");
        });

        Assertions.assertEquals(ex.getMessage(), "Credenciales inválidas");
    }

    @Test
    void testCreateUser() {
        String encodedPass = "encodedPass";
        User user = createTestUser(false);
        User userPassEncoded = encodePass(user);
        
        Mockito.when(repo.save(userPassEncoded)).thenReturn(userPassEncoded);
        Mockito.when(repo.existsByEmail(user.email())).thenReturn(false);
        Mockito.when(passEncoder.encode(user.password())).thenReturn(encodedPass);

        UserDto userDto = service.createUser(user);
         
        Assertions.assertNotNull(userDto);
        Assertions.assertEquals(userDto.getIdentification(), user.identification());
        Assertions.assertEquals(userDto.getPassword(), encodedPass);
    }
    
    @Test
    void testCreateUserEmailAlreadyExists() {
        User user = createTestUser(false);
        Mockito.when(repo.existsByEmail(user.email())).thenReturn(true);
        
        UserAlreadyExistsException ex = Assertions.assertThrows(UserAlreadyExistsException.class, () -> {
            service.createUser(user);
        });

        Assertions.assertEquals(ex.getMessage(), "El usuario con este email ya existe");
    }

    User createTestUser(boolean encodedPass) {
        Instant now = Instant.now();
        return User.builder()
            .email("jane.doe@example.com")
            .userType(UserType.CUSTOMER)
            .authType(UserAuthType.PASSWORD)
            .identification("ID-123")
            .name("Jane Doe")
            .password(encodedPass ? "encodedPass" : "not-a-real-password")
            .status(0)
            .registeredAt(now)
            .lastLoginAt(now)
            .build();
    }
    
    User encodePass(User user) {
        return User.builder()
                .userId(user.userId())
                .identification(user.identification())
                .name(user.name())
                .email(user.email())
                .password("encodedPass")
                .authType(user.authType())
                .status(user.status())
                .userType(user.userType())
                .registeredAt(user.registeredAt())
                .build();
    }
}
