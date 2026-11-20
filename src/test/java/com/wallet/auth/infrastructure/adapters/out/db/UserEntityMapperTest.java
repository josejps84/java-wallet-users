package com.wallet.auth.infrastructure.adapters.out.db;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.model.UserAuthType;
import com.wallet.auth.domain.model.UserType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityMapperTest {

    @Test
    void roundTripKeepsEveryField() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        User user = new User(5L, "ID-5", "Jane Doe", "jane@example.com", "hash", UserAuthType.OAUTH, 1,
                UserType.ADMIN, now, now.plusSeconds(60));

        UserEntity entity = UserEntityMapper.toEntity(user);

        assertThat(entity.getAuthType()).isEqualTo(UserAuthType.OAUTH.ordinal());
        assertThat(entity.getUserType()).isEqualTo(UserType.ADMIN.ordinal());
        assertThat(UserEntityMapper.toDomain(entity)).isEqualTo(user);
    }
}
