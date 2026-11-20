package com.wallet.auth.domain.ports.in;

import com.wallet.auth.domain.model.User;

public interface CreateUserUseCase {

    /** Registers the user and opens their wallet account. */
    User createUser(User user);
}
