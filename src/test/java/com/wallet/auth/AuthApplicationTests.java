package com.wallet.auth;

import com.wallet.auth.domain.ports.out.AccountRegistrationPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Registration and login against a real MongoDB container; skipped when Docker is not available. */
@SpringBootTest(properties = "jwt.secret=test-secret-test-secret-test-secret-1234")
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthApplicationTests {

    @Container
    @ServiceConnection
    static MongoDBContainer mongo = new MongoDBContainer("mongo:8");

    @Autowired
    MockMvc mvc;

    @MockitoBean
    AccountRegistrationPort accountRegistration;

    @Test
    void registerLoginAndValidation() throws Exception {
        String body = """
                {"identification":"ID-1","name":"Jane Doe","email":"jane@example.com","password":"long-enough-pw"}""";

        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.identification").value("ID-1"))
                .andExpect(jsonPath("$.password").doesNotExist());

        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body.replace("jane@", "JANE@")))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jane@example.com\",\"password\":\"long-enough-pw\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        mvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"jane@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Credenciales inválidas"));

        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identification\":\"\",\"name\":\"x\",\"email\":\"bad\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());

        mvc.perform(get("/api/users")).andExpect(status().isForbidden());
    }
}
