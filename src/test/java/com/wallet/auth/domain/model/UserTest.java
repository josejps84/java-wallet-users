package com.wallet.auth.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void createdAtIsAssignedAutomaticallyWhenUserIsCreated() {
        User user = new User("test@example.com", "Test User", "password");

        assertThat(user.createdAt()).isNotNull();
    }
}
