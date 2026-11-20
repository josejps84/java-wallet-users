package com.wallet.auth.infrastructure.adapters.out.accounts;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.model.exception.AccountRegistrationFailedException;
import com.wallet.auth.domain.ports.out.AccountRegistrationPort;
import com.wallet.auth.domain.ports.out.TokenProvider;

/**
 * Opens the user's account by calling the account service directly, authenticated as the new user, so the account
 * service assigns it to the user id inside the token.
 */
@Component
public class HttpAccountRegistrationAdapter implements AccountRegistrationPort {

    private final RestClient restClient;
    private final TokenProvider tokenProvider;
    private final RetryTemplate retryTemplate;

    public HttpAccountRegistrationAdapter(@Value("${app.accounts.base-url}") String baseUrl,
                                          TokenProvider tokenProvider) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        this.tokenProvider = tokenProvider;
        // Only network failures are retried (1s, then 2s); HTTP error answers fail immediately
        this.retryTemplate = new RetryTemplate(RetryPolicy.builder()
                .includes(ResourceAccessException.class)
                .maxRetries(2)
                .delay(Duration.ofSeconds(1))
                .multiplier(2)
                .build());
    }

    @Override
    public void register(User user) {
        String token = tokenProvider.generateToken(user.email(), user.userType().name(), user.userId());
        try {
            retryTemplate.execute(() -> restClient.post()
                    .uri("/api/accounts/new")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of())
                    .retrieve()
                    .toBodilessEntity());
        } catch (RetryException ex) {
            throw new AccountRegistrationFailedException(
                    "No se pudo crear la cuenta en el servicio de cuentas. Intenta de nuevo.", ex.getCause());
        }
    }
}
