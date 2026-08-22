# AGENTS.md

Spring Boot 4.1.0 / Java 25 auth service (`com.wallet:auth`). Maven wrapper only; single module. No CI, lint, or formatter config.

## Build & test (JDK gotcha)

`mvnw` is not executable — run it via `bash mvnw ...` (or `chmod +x mvnw` once).

Build **only works on JDK 25** (matches `pom.xml` `java.version` and the Dockerfile's temurin-25). On newer local JDKs (e.g. 27), Lombok breaks: by default annotation processing silently doesn't run (massive `cannot find symbol` errors), and forcing `-Dmaven.compiler.proc=full` crashes Lombok itself. Always pin:

```sh
JAVA_HOME=/usr/lib/jvm/java-25-openjdk bash mvnw test        # full suite
JAVA_HOME=/usr/lib/jvm/java-25-openjdk bash mvnw test -Dtest=UserTest   # single test
JAVA_HOME=/usr/lib/jvm/java-25-openjdk bash mvnw spring-boot:run        # dev server on :8081
```

Tests do NOT require a running MongoDB (`@SpringBootTest` context loads with an unreachable Mongo URI — verified).

## Architecture (hexagonal-ish)

- `domain/ports/in` = use-case interfaces; `domain/ports/out` = repository/password/token ports.
- `application/services/UserService` implements the use cases; inject new behavior through ports, not concrete classes.
- `infrastructure/adapters/in/web` (controllers, DTOs, exception handler) and `infrastructure/adapters/out/db` (Mongo repo, BCrypt) implement the ports. Entry point: `AuthApplication`.
- Existing quirks to preserve, not fix casually: package is misspelled `domain.model.excecption`, and `UserService` imports `UserDto` from infrastructure.

## Runtime config quirks

- `application.properties`: port is **8081** locally, but the Dockerfile only `EXPOSE`s 8080 without overriding it — containers actually listen on 8081.
- Mongo URI property is `spring.mongodb.uri` (Boot 4 name). The Dockerfile resolves env `SPRING_MONGODB_URI` / `SPRING_DATA_MONGODB_URI` into both `spring.mongodb.uri` and `spring.data.mongodb.uri`, defaulting to `mongodb://mongo:27017/wallet`.
- JWT keys: `app.jwt.secret`, `app.jwt.expiration-ms` (read by `JwtTokenProvider`).
- Security (`AppConfig`): CSRF disabled, `/api/users/**` fully public, all else requires auth; CORS allows `localhost:3000` and `localhost:5173` with credentials. Endpoints: `POST /api/users/register`, `POST /api/users/login` (returns `{token}`).
