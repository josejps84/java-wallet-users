package com.wallet.auth;

import com.wallet.auth.domain.model.User;
import com.wallet.auth.domain.model.UserAuthType;
import com.wallet.auth.domain.model.UserType;
import com.wallet.auth.domain.model.exception.AccountRegistrationFailedException;
import com.wallet.auth.domain.model.exception.UserAlreadyExistsException;
import com.wallet.auth.domain.ports.out.AccountRegistrationPort;
import com.wallet.auth.domain.ports.out.UserRepositoryPort;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Registration and login against a real MongoDB container; skipped when Docker is not available. */
@SpringBootTest(properties = "jwt.secret=" + AuthApplicationTests.SECRET)
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthApplicationTests {

    static final String SECRET = "test-secret-test-secret-test-secret-1234";

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8");

    @Autowired
    MockMvc mvc;

    @Autowired
    UserRepositoryPort userRepository;

    @MockitoBean
    AccountRegistrationPort accountRegistration;

    @Test
    void registerLoginAndValidation() throws Exception {
        String email = uniqueEmail();

        register(email, "long-enough-pw")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identification").value("ID-1"))
                .andExpect(jsonPath("$.password").doesNotExist());

        register(email.toUpperCase(), "long-enough-pw").andExpect(status().isConflict());

        login(email, "long-enough-pw").andExpect(status().isOk()).andExpect(jsonPath("$.token").isNotEmpty());
        login(email, "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Credenciales inválidas"));

        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identification\":\"\",\"name\":\"x\",\"email\":\"bad\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());

        mvc.perform(get("/api/users")).andExpect(status().isForbidden());
    }

    @Test
    void issuedTokenIdentifiesTheRegisteredUser() throws Exception {
        String email = uniqueEmail();
        String body = register(email, "long-enough-pw").andReturn().getResponse().getContentAsString();
        long userId = Long.parseLong(body.replaceAll(".*\"userId\":(\\d+).*", "$1"));

        String loginBody = login(email, "long-enough-pw").andReturn().getResponse().getContentAsString();
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(loginBody.replaceAll(".*\"token\":\"([^\"]+)\".*", "$1"))
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo(email);
        assertThat(claims.get("userId", Long.class)).isEqualTo(userId);
        assertThat(claims.get("userType", String.class)).isEqualTo("CUSTOMER");

        // The account service is asked to open the wallet of that same, already persisted, user
        ArgumentCaptor<User> registered = ArgumentCaptor.forClass(User.class);
        verify(accountRegistration).register(registered.capture());
        assertThat(registered.getValue().userId()).isEqualTo(userId);
    }

    @Test
    void userIdsAreSequential() throws Exception {
        long first = registeredId(uniqueEmail());
        long second = registeredId(uniqueEmail());

        assertThat(first).isPositive();
        assertThat(second).isEqualTo(first + 1);
    }

    @Test
    void registrationIsRolledBackWhenTheAccountCannotBeOpened() throws Exception {
        String email = uniqueEmail();
        doThrow(new AccountRegistrationFailedException("No se pudo crear la cuenta"))
                .when(accountRegistration).register(any());

        register(email, "long-enough-pw")
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.detail").value("No se pudo crear la cuenta"));

        assertThat(userRepository.existsByEmail(email)).isFalse();
        login(email, "long-enough-pw").andExpect(status().isUnauthorized());
    }

    @Test
    void uniqueEmailIndexStopsDuplicatesThatSkipTheExistenceCheck() {
        String email = uniqueEmail();
        userRepository.save(user(email));

        assertThatThrownBy(() -> userRepository.save(user(email))).isInstanceOf(UserAlreadyExistsException.class);
    }

    private ResultActions register(String email, String password) throws Exception {
        return mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"identification\":\"ID-1\",\"name\":\"Jane Doe\",\"email\":\"" + email
                        + "\",\"password\":\"" + password + "\"}"));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
    }

    private long registeredId(String email) throws Exception {
        String body = register(email, "long-enough-pw").andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll(".*\"userId\":(\\d+).*", "$1"));
    }

    private static User user(String email) {
        return User.builder().email(email).name("Jane").identification("ID").password("hash")
                .authType(UserAuthType.PASSWORD).userType(UserType.CUSTOMER).registeredAt(Instant.now()).build();
    }

    private static String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }
}
