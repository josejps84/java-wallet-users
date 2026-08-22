package com.wallet.auth.infrastructure.adapters.out.db;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.ports.out.UserRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class MongoUserRepositoryAdapter implements UserRepositoryPort {

    private final SpringDataMongoUserRepository repository;

    public MongoUserRepositoryAdapter(SpringDataMongoUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User save(User user) {
        UserEntity entity = new UserEntity(user.email(), user.name(), user.password(), user.createdAt());
        UserEntity saved = repository.save(entity);
        return new User(saved.getEmail(), saved.getName(), saved.getPassword(), saved.getCreatedAt());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(entity -> new User(entity.getEmail(), entity.getName(), entity.getPassword(), entity.getCreatedAt()));
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
