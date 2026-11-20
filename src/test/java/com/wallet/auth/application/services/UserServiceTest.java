package com.wallet.auth.application.services;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.model.UserAuthType;
import com.wallet.auth.domain.model.UserType;
import com.wallet.auth.domain.model.exception.AccountRegistrationFailedException;
import com.wallet.auth.domain.model.exception.InvalidCredentialsException;
import com.wallet.auth.domain.model.exception.UserAlreadyExistsException;
import com.wallet.auth.domain.ports.out.AccountRegistrationPort;
import com.wallet.auth.domain.ports.out.PasswordEncoderPort;
import com.wallet.auth.domain.ports.out.TokenProvider;
import com.wallet.auth.domain.ports.out.UserRepositoryPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String EMAIL = "jane.doe@example.com";
    private static final String RAW_PASSWORD = "not-a-real-password";
    private static final String HASH = "hashed-password";

    @Mock
    UserRepositoryPort repo;
    @Mock
    PasswordEncoderPort passEncoder;
    @Mock
    TokenProvider tokenProvider;
    @Mock
    AccountRegistrationPort accountRegistration;

    @InjectMocks
    UserService service;

    @Test
    void authenticateReturnsTokenForValidCredentials() {
        User stored = testUser(7L, HASH);
        when(repo.findByEmail(EMAIL)).thenReturn(Optional.of(stored));
        when(passEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(true);
        when(tokenProvider.generateToken(EMAIL, "CUSTOMER", 7L)).thenReturn("token");

        assertThat(service.authenticate(" Jane.Doe@Example.com ", RAW_PASSWORD)).isEqualTo("token");
    }

    @Test
    void authenticateRejectsWrongPassword() {
        when(repo.findByEmail(EMAIL)).thenReturn(Optional.of(testUser(7L, HASH)));
        when(passEncoder.matches(RAW_PASSWORD, HASH)).thenReturn(false);

        assertThatThrownBy(() -> service.authenticate(EMAIL, RAW_PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void authenticateRejectsUnknownEmail() {
        when(repo.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.authenticate(EMAIL, RAW_PASSWORD))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void createUserHashesPasswordAndOpensAccount() {
        when(repo.existsByEmail(EMAIL)).thenReturn(false);
        when(passEncoder.encode(RAW_PASSWORD)).thenReturn(HASH);
        when(repo.save(any())).thenAnswer(invocation -> ((User) invocation.getArgument(0)).toBuilder().userId(7L).build());

        User created = service.createUser(testUser(0L, RAW_PASSWORD).toBuilder().email("JANE.DOE@example.com").build());

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(repo).save(saved.capture());
        assertThat(saved.getValue().password()).isEqualTo(HASH);
        assertThat(saved.getValue().email()).isEqualTo(EMAIL);
        assertThat(saved.getValue().identification()).isEqualTo("ID-123");
        assertThat(created.userId()).isEqualTo(7L);
        verify(accountRegistration).register(created);
    }

    @Test
    void createUserRollsBackWhenAccountCannotBeOpened() {
        when(repo.existsByEmail(EMAIL)).thenReturn(false);
        when(passEncoder.encode(RAW_PASSWORD)).thenReturn(HASH);
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new AccountRegistrationFailedException("down")).when(accountRegistration).register(any());

        assertThatThrownBy(() -> service.createUser(testUser(0L, RAW_PASSWORD)))
                .isInstanceOf(AccountRegistrationFailedException.class);
        verify(repo).deleteByEmail(EMAIL);
    }

    @Test
    void createUserRejectsDuplicatedEmail() {
        when(repo.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> service.createUser(testUser(0L, RAW_PASSWORD)))
                .isInstanceOf(UserAlreadyExistsException.class);
        verify(repo, never()).save(any());
    }

    private static User testUser(long userId, String password) {
        return User.builder()
                .userId(userId)
                .email(EMAIL)
                .userType(UserType.CUSTOMER)
                .authType(UserAuthType.PASSWORD)
                .identification("ID-123")
                .name("Jane Doe")
                .password(password)
                .registeredAt(Instant.now())
                .build();
    }
}
