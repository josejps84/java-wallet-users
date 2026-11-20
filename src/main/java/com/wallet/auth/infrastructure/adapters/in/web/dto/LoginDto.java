package com.wallet.auth.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginDto(@NotBlank String email, @NotBlank String password) {
}
