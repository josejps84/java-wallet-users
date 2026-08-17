package com.wallet.auth.domain.ports.in;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.infrastructure.adapters.in.web.dto.UserDto;

public interface CreateUserUseCase {
    UserDto createUser(User user);
}