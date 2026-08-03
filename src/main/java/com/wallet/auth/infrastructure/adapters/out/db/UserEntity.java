package com.wallet.auth.infrastructure.adapters.out.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "users")
public class UserEntity {
    @Id
    private String email;
    private String name;
    private String password;
    private Instant createdAt;

    public UserEntity() {}

    public UserEntity(String email, String name, String password, Instant createdAt) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.createdAt = createdAt;
    }

    public UserEntity(String email, String password) {
        this(email, null, password, Instant.now());
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}