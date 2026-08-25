package com.wallet.auth.infrastructure.adapters.out.db;
import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.model.UserAuthType;
import com.wallet.auth.domain.model.UserType;

public class UserEntityMapper {
    public static UserEntity toEntity(User user) {
        return UserEntity.builder()
                .userId(user.userId())
                .identification(user.identification())
                .name(user.name())
                .email(user.email())
                .password(user.password())
                .authType(user.authType().ordinal())
                .status(user.status())
                .userType(user.userType().ordinal())
                .registeredAt(user.registeredAt())
                .lastLoginAt(user.lastLoginAt())
                .build();
    }
    
    public static User toDomain(UserEntity entity) {
        return User.builder()
                .userId(entity.getUserId())
                .identification(entity.getIdentification())
                .name(entity.getName())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .authType(UserAuthType.values()[entity.getAuthType()])
                .status(entity.getStatus())
                .userType(UserType.values()[entity.getUserType()])
                .registeredAt(entity.getRegisteredAt())
                .lastLoginAt(entity.getLastLoginAt())
                .build();
    }
}
