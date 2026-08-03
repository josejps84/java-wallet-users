package com.wallet.auth.domain.ports.in;

import com.wallet.auth.domain.model.User;

public interface CreateUserUseCase {
    User createUser(User user);
}