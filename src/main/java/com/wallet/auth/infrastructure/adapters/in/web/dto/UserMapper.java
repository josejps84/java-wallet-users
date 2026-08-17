package com.wallet.auth.infrastructure.adapters.in.web.dto;

import com.wallet.auth.domain.model.User;

public class UserMapper {

    public static UserDto toDto(User user) {
        return UserDto.builder()
                .email(user.email())
                .name(user.name())
                .password(user.password())
                .createdAt(user.createdAt())
                .build();
    }

    public static User toDomain(UserDto userDto) {
        return User.builder()
                .email(userDto.getEmail())
                .name(userDto.getName())
                .createdAt(userDto.getCreatedAt())
                .build();
    }
    
}
