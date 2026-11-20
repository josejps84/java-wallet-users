package com.wallet.auth.infrastructure.adapters.in.web.dto;

/** Public view of a user; never carries the password hash. */
public record UserDto(long userId, String identification, String name, String email) {
}
