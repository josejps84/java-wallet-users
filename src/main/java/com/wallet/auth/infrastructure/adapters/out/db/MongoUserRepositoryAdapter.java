package com.wallet.auth.infrastructure.adapters.out.db;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.model.exception.UserAlreadyExistsException;
import com.wallet.auth.domain.ports.out.UserRepositoryPort;
import org.springframework.dao.DuplicateKeyException;
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
        UserEntity entity = UserEntityMapper.toEntity(user);
        if (entity.getUserId() == 0) {
            entity.setUserId(userIdGenerator.nextUserId());
        }
        try {
            return UserEntityMapper.toDomain(repository.save(entity));
        } catch (DuplicateKeyException ex) {
            // Unique index on email: two concurrent registrations with the same email
            throw new UserAlreadyExistsException("El usuario con este email ya existe");
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(UserEntityMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    @Override
    public void deleteByEmail(String email) {
        repository.deleteByEmail(email);
    }
}
