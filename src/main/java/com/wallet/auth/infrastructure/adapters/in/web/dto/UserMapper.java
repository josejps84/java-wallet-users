package com.wallet.auth.infrastructure.adapters.in.web.dto;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.model.UserAuthType;
import com.wallet.auth.domain.model.UserType;

import java.time.Instant;

public class UserMapper {

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .email(user.email())
                .name(user.name())
                .authType(user.authType().ordinal())
                .build();
            }
            
            public static User toDomain(UserDto userDto) {
                return User.builder()
                .email(userDto.getEmail())
                .name(userDto.getName())
                .password(userDto.getPassword())
                .authType(UserAuthType.values()[userDto.getAuthType()])
                .userType(UserType.CUSTOMER)
                .status(0)
                .registeredAt(Instant.now())
                .build();
    }
    
}
