package com.wallet.auth.infrastructure.adapters.in.web;

import com.wallet.auth.domain.ports.in.AuthenticateUserUseCase;
import com.wallet.auth.domain.ports.in.CreateUserUseCase;
import com.wallet.auth.infrastructure.adapters.in.web.dto.LoginDto;
import com.wallet.auth.infrastructure.adapters.in.web.dto.RegisterUserDto;
import com.wallet.auth.infrastructure.adapters.in.web.dto.UserDto;
import com.wallet.auth.infrastructure.adapters.in.web.dto.UserMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public UserController(CreateUserUseCase createUserUseCase, AuthenticateUserUseCase authenticateUserUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto register(@Valid @RequestBody RegisterUserDto request) {
        return UserMapper.toDto(createUserUseCase.createUser(UserMapper.toDomain(request)));
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginDto request) {
        return Map.of("token", authenticateUserUseCase.authenticate(request.email(), request.password()));
    }
}
