package com.wallet.auth.domain.model;

import java.time.Instant;

import lombok.Builder;

@Builder
public record User(String email, String name, String password, Instant createdAt) {

    public User(String email, String name, String password) {
        this(email, name, password, Instant.now());
    }

    public User(String email, String password) {
        this(email, null, password, Instant.now());
    }
}