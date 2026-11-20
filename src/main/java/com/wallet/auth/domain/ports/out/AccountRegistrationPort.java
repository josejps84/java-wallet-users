package com.wallet.auth.domain.ports.out;

import com.wallet.auth.domain.model.User;

public interface AccountRegistrationPort {

    /**
     * Opens the default wallet account of a newly registered user.
     *
     * @throws com.wallet.auth.domain.model.exception.AccountRegistrationFailedException if it can't be created
     */
    void register(User user);
}
