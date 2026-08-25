package com.wallet.auth.domain.model;

import java.time.Instant;

import lombok.Builder;

@Builder
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
    
    public User(String identification, String name, String email, String password, int authType, int userType, Instant createdAt) {
        this(0L, identification, name, email, password, UserAuthType.values()[authType], 0, UserType.values()[userType], createdAt, null);
    }
    
}