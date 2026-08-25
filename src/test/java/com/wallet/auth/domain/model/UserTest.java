package com.wallet.auth.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

class UserTest {

    @Test
    void createdAtIsAssignedAutomaticallyWhenUserIsCreated() {
        User user = new User("ID-123", "John Doe", "john.doe@example.com", "password", 0, 0, Instant.now());

        assertThat(user.registeredAt()).isNotNull();
    }
}
