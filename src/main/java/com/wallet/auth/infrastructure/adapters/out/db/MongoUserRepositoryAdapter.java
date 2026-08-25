package com.wallet.auth.infrastructure.adapters.out.db;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.model.UserAuthType;
import com.wallet.auth.domain.model.UserType;
import com.wallet.auth.domain.ports.out.UserRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class MongoUserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataMongoUserRepository repository;
    private final UserIdGenerator userIdGenerator;

    public MongoUserRepositoryAdapter(SpringDataMongoUserRepository repository, UserIdGenerator userIdGenerator) {
        this.repository = repository;
        this.userIdGenerator = userIdGenerator;
    }

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.builder()
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
        entity.setUserId(userIdGenerator.nextUserId());

        UserEntity saved = repository.save(entity);
        return User.builder()
                .userId(saved.getUserId())
                .identification(saved.getIdentification())
                .name(saved.getName())
                .email(saved.getEmail())
                .password(saved.getPassword())
                .authType(UserAuthType.values()[saved.getAuthType()])
                .status(saved.getStatus())
                .userType(UserType.values()[saved.getUserType()])
                .registeredAt(saved.getRegisteredAt())
                .lastLoginAt(saved.getLastLoginAt())
                .build();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(UserEntityMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public void deleteByEmail(String email) {
        repository.findByEmail(email).ifPresent(repository::delete);
    }
}
