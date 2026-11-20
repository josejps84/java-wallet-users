package com.wallet.auth.domain.ports.out;

public interface TokenProvider {
    String generateToken(String email, String userType, long userId);
}
