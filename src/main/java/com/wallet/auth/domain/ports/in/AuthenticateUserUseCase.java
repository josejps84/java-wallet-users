package com.wallet.auth.domain.ports.in;

public interface AuthenticateUserUseCase {
    boolean authenticate(String email, String password);
}