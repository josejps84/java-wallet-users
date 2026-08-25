package com.wallet.auth.infrastructure.adapters.out.db;
import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.model.UserAuthType;
import com.wallet.auth.domain.model.UserType;

public class UserEntityMapper {
    public static UserEntity toEntity(User user) {
        return new UserEntity(user.userId(), user.identification(), user.name(), user.email(), user.password(), user.authType().ordinal(), user.status(), user.userType().ordinal(), user.registeredAt(), user.lastLoginAt());
    }
    
    public static User toDomain(UserEntity entity) {
        return new User(entity.getUserId(), entity.getIdentification(), entity.getName(), entity.getEmail(), entity.getPassword(), UserAuthType.values()[entity.getAuthType()], entity.getStatus(), UserType.values()[entity.getUserType()], entity.getRegisteredAt(), entity.getLastLoginAt());
    }
}
