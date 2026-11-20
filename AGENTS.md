# AGENTS.md

Spring Boot 4.1 / Java 25 users & auth service (`com.wallet:auth`, packages `com.wallet.auth`). Maven wrapper only; single module.

## Build & test

Build **only works on JDK 25**: on newer JDKs (e.g. 27) Lombok crashes. Lombok runs through `annotationProcessorPaths` in `pom.xml`.

```sh
JAVA_HOME=/usr/lib/jvm/java-25-openjdk ./mvnw test                        # full suite
JAVA_HOME=/usr/lib/jvm/java-25-openjdk ./mvnw test -Dtest=UserServiceTest  # single test
JAVA_HOME=/usr/lib/jvm/java-25-openjdk ./mvnw spring-boot:run              # dev server on :8081
```

`AuthApplicationTests` starts MongoDB with Testcontainers (skipped without Docker). Never put real personal data in tests.

## Architecture (hexagonal)

- `domain/ports/in` = use cases (`CreateUserUseCase`, `AuthenticateUserUseCase`); `domain/ports/out` = repository, password encoder, `TokenProvider`, `AccountRegistrationPort`. Domain and application code must not import infrastructure classes (DTOs live in `infrastructure/adapters/in/web/dto`).
- `UserService` registers the user, then opens their wallet through `AccountRegistrationPort`; if that fails the user is deleted again (502 to the client).
- `HttpAccountRegistrationAdapter` calls the account service directly (`app.accounts.base-url`) with a JWT minted for the new user, retrying only network errors (Spring Framework `RetryTemplate`).
- Emails are normalized (trim + lowercase) and have a unique Mongo index (`spring.data.mongodb.auto-index-creation=true`).

## Runtime config

- Port **8081**. Endpoints: `POST /api/users/register` (201 + user without password), `POST /api/users/login` (`{token}`, 401 on bad credentials). Everything else is denied.
- Env vars: `JWT_SECRET` (required, same as account service and gateway), `MONGODB_URI`, `ACCOUNTS_BASE_URL`, `JWT_EXPIRATION_MS`. Local runs read them from `.env` (git-ignored, see `.env.example`).
- Errors are RFC 9457 `ProblemDetail` JSON; user-facing messages in Spanish, logs in English.
