package com.wallet.auth.domain.model.exception;

public class AccountRegistrationFailedException extends RuntimeException {

    public AccountRegistrationFailedException(String message) {
        super(message);
    }

    public AccountRegistrationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
