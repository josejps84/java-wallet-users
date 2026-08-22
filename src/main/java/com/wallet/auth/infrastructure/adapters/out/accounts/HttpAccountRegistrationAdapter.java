package com.wallet.auth.infrastructure.adapters.out.accounts;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.wallet.auth.domain.model.exception.AccountRegistrationFailedException;
import com.wallet.auth.domain.ports.out.AccountRegistrationPort;

@Component
public class HttpAccountRegistrationAdapter implements AccountRegistrationPort {

    private final RestClient restClient;

    public HttpAccountRegistrationAdapter(@Value("${app.accounts.base-url:http://localhost:8082}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public void register(String email) {
        try {
            HttpStatusCode statusCode = restClient.post()
                    .uri("/api/accounts/new")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", email))
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode();

            if (!statusCode.is2xxSuccessful()) {
                throw new AccountRegistrationFailedException("La creación de la cuenta falló en el microservicio de cuentas.");
            }
        } catch (Exception ex) {
            if (ex instanceof AccountRegistrationFailedException accountRegistrationFailedException) {
                throw accountRegistrationFailedException;
            }
            throw new AccountRegistrationFailedException("La creación de la cuenta falló en el microservicio de cuentas.", ex);
        }
    }
}
