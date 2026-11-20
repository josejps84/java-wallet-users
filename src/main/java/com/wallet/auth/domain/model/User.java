package com.wallet.auth.domain.model;

import java.time.Instant;

import lombok.Builder;

/**
 * @param userId 0 until the user has been persisted
 * @param password the BCrypt hash once stored; the raw password only while registering
 */
@Builder(toBuilder = true)
public record User(long userId,
    String identification,
    String name,
    String email,
    String password,
    UserAuthType authType,
    int status,
    UserType userType,
    Instant registeredAt,
    Instant lastLoginAt) {

    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
