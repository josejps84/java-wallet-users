package com.wallet.auth.infrastructure.adapters.out.accounts;

import com.sun.net.httpserver.HttpServer;
import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.model.UserType;
import com.wallet.auth.domain.model.exception.AccountRegistrationFailedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Talks to a stub account service over real HTTP. */
class HttpAccountRegistrationAdapterTest {

    private final AtomicInteger attempts = new AtomicInteger();
    private final AtomicReference<String> lastRequest = new AtomicReference<>();
    private volatile int status = 201;
    private volatile boolean dropConnection;

    private HttpServer server;
    private HttpAccountRegistrationAdapter adapter;

    private final User user = User.builder().userId(42L).email("jane@example.com").userType(UserType.CUSTOMER).build();

    @BeforeEach
    void startStub() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            attempts.incrementAndGet();
            if (dropConnection) {
                exchange.close();   // no response at all: a network failure for the client
                return;
            }
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            lastRequest.set(exchange.getRequestMethod() + " " + exchange.getRequestURI() + " "
                    + exchange.getRequestHeaders().getFirst("Authorization") + " " + body);
            exchange.sendResponseHeaders(status, -1);
            exchange.close();
        });
        server.start();
        adapter = new HttpAccountRegistrationAdapter("http://localhost:" + server.getAddress().getPort(),
                (email, userType, userId) -> "token-" + email + "-" + userType + "-" + userId);
    }

    @AfterEach
    void stopStub() {
        server.stop(0);
    }

    @Test
    void opensTheAccountAuthenticatedAsTheNewUser() {
        adapter.register(user);

        assertThat(lastRequest.get())
                .isEqualTo("POST /api/accounts/new Bearer token-jane@example.com-CUSTOMER-42 {}");
        assertThat(attempts).hasValue(1);
    }

    @Test
    void httpErrorsFailImmediatelyWithoutRetrying() {
        status = 500;

        assertThatThrownBy(() -> adapter.register(user)).isInstanceOf(AccountRegistrationFailedException.class);
        assertThat(attempts).hasValue(1);
    }

    @Test
    void networkFailuresAreRetriedTwiceBeforeGivingUp() {
        dropConnection = true;

        assertThatThrownBy(() -> adapter.register(user))
                .isInstanceOf(AccountRegistrationFailedException.class)
                .hasMessageContaining("Intenta de nuevo");
        assertThat(attempts).hasValue(3);
    }
}
