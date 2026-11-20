package com.wallet.auth.infrastructure.adapters.out.db;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class UserEntity {
    @Id
    private long userId;
    private String identification;
    private String name;
    @Indexed(unique = true)
    private String email;
    private String password;
    private int authType;
    private int status;
    private int userType;
    private Instant registeredAt;
    private Instant lastLoginAt;
}
