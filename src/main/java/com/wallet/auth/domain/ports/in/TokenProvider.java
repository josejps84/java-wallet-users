package com.wallet.auth.domain.ports.in;

public interface TokenProvider {
    String generateToken(String email, String userType);
}
