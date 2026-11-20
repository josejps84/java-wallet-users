package com.wallet.auth.infrastructure.adapters.out.db;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataMongoUserRepository extends MongoRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    void deleteByEmail(String email);
}
