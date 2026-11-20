package com.wallet.auth.domain.ports.in;

public interface AuthenticateUserUseCase {

    /**
     * @return a signed JWT for the user
     * @throws com.wallet.auth.domain.model.exception.InvalidCredentialsException if email or password don't match
     */
    String authenticate(String email, String password);
}
