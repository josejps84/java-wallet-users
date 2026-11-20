package com.wallet.auth.infrastructure.adapters.in.web.dto;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.model.UserAuthType;
import com.wallet.auth.domain.model.UserType;

import java.time.Instant;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        return new UserDto(user.userId(), user.identification(), user.name(), user.email());
    }

    public static User toDomain(RegisterUserDto dto) {
        return User.builder()
                .identification(dto.identification().trim())
                .name(dto.name().trim())
                .email(dto.email())
                .password(dto.password())
                .authType(UserAuthType.PASSWORD)
                .userType(UserType.CUSTOMER)
                .status(0)
                .registeredAt(Instant.now())
                .build();
    }
}
