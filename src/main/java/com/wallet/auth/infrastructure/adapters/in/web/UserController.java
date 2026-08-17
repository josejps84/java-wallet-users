package com.wallet.auth.infrastructure.adapters.in.web;
import com.wallet.auth.infrastructure.adapters.in.web.dto.UserMapper;
import com.wallet.auth.domain.ports.in.AuthenticateUserUseCase;
import com.wallet.auth.domain.ports.in.CreateUserUseCase;
import com.wallet.auth.infrastructure.adapters.in.web.dto.UserDto;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> register(@RequestBody UserDto request) {
        createUserUseCase.createUser(UserMapper.toDomain(request));
        return ResponseEntity.ok("Usuario creado exitosamente");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserDto request) {
        return authenticateUserUseCase.authenticate(request.getEmail(), request.getPassword())
                .map(token -> ResponseEntity.ok(Map.of("token", token)))
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("error", "Credenciales inválidas")));
    }
}