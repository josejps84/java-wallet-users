package com.wallet.auth.domain.ports.in;

import java.util.Optional;

public interface AuthenticateUserUseCase {
    Optional<String> authenticate(String email, String password);
}