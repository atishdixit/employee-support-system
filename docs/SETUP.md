# Setup

## Prerequisites

- **Java 21+** (JDK)
- **Maven 3.9+**
- **Node.js 18+** and npm
- **[Ollama](https://ollama.com)** installed, with a chat-capable model pulled (default:
  `llama3.2:3b` — `setup.bat` pulls it automatically if missing)

## Environment variables

Copy `.env.example` to `.env` at the repo root and adjust as needed. Both the backend
(via [spring-dotenv](https://github.com/paulschwarz/spring-dotenv), which loads `.env`
automatically) and `docker-compose` (via `env_file`) read the same file.

| Variable                | Default                   | Meaning                                              |
|--------------------------|----------------------------|-------------------------------------------------------|
| `OLLAMA_BASE_URL`        | `http://localhost:11434`  | Where Ollama is listening                             |
| `OLLAMA_MODEL`           | `llama3.2:3b`              | Model name, used for both the connection and options  |
| `CHAT_PROVIDER`          | `ollama`                   | Label only today — see "Adding a new LLM provider"    |
| `CHAT_TEMPERATURE`       | `0.4`                       | Portable `ChatOptions` temperature                     |
| `CHAT_MAX_TOKENS`        | `300`                       | Ceiling on one answer's length                        |
| `CHAT_MIN_TOKENS`        | `20`                        | Floor — startup fails if `MAX_TOKENS < MIN_TOKENS`    |
| `SPRING_PROFILES_ACTIVE` | `dev`                       | `dev` \| `test` \| `stage` \| `prod`                  |
| `API_PORT`               | `8080`                      | Backend port                                          |
| `UI_PORT`                | `4200`                      | Frontend port                                         |
| `UI_ORIGIN`              | `http://localhost:4200`    | Allowed CORS origin on the backend                    |
| `JWT_SECRET`             | a demo value (see `.env.example`) | HMAC signing key, must be ≥32 characters; startup fails otherwise |
| `JWT_EXPIRATION_MINUTES` | `60`                        | How long an issued token stays valid                   |

## Demo logins

There is no user database — `DemoUserService` hardcodes one login per seeded employee. All 5
share the same password.

| Username | Employee                                | Password     |
|----------|-------------------------------------------|---------------|
| `e001`   | Asha Verma (Senior Software Engineer)     | `Passw0rd!`   |
| `e002`   | Rohan Mehta (Account Executive)           | `Passw0rd!`   |
| `e003`   | Priya Nair (HR Business Partner)          | `Passw0rd!`   |
| `e004`   | Karan Singh (Engineering Manager)         | `Passw0rd!`   |
| `e005`   | Meera Iyer (Financial Analyst)            | `Passw0rd!`   |

`POST /api/v1/auth/login` with `{"username": "...", "password": "..."}` returns a JWT; send it
as `Authorization: Bearer <token>` on every other `/api/v1/**` call (the Angular app's
`authInterceptor` does this automatically after login). These are demo-only, hardcoded, throwaway
credentials — never reuse this pattern for real user accounts.

## API docs (Swagger)

`http://localhost:8080/swagger-ui/index.html` once the backend is running. Click **Authorize**
and paste a JWT from `/api/v1/auth/login` to try out the protected endpoints directly from the
UI. The raw OpenAPI document is at `/v3/api-docs`.

## Profiles

- **dev** (`application-dev.yml`) — `DEBUG` logging for `com.ext.emp.support`, meant for local
  development.
- **test** (`application-test.yml`) — smaller `max-tokens` (150) for faster manual/automated
  checks.
- **stage** (`application-stage.yml`) — quieter logging, otherwise identical to prod.
- **prod** (`application-prod.yml`) — `WARN`-level console logging; relies on the Log4j2
  rolling file appender (`logs/employee-support-app.log`) for detail.

Select a profile via `SPRING_PROFILES_ACTIVE` in `.env`, or
`--spring.profiles.active=<profile>` on the command line (this overrides `.env`).

## Running locally (no Docker)

```bash
cd backend
mvn install -DskipTests
java -jar employee-support-app/target/employee-support-app.jar --spring.profiles.active=dev
```

```bash
cd frontend
npm install
npx ng serve
```

Or just run `setup.bat` once and `run.bat` any time (Windows).

## Adding a new LLM provider

Only `spring-ai-starter-model-ollama` is on the classpath today, so `CHAT_PROVIDER` is a label,
not a live switch. To add a second provider (e.g. OpenAI):

1. Add its starter to `backend/employee-support-app/pom.xml`
   (e.g. `spring-ai-starter-model-openai`).
2. Both starters auto-configure a `ChatClient.Builder` bean; when two are present, qualify
   which one `ChatClientConfig.chatClient(...)` should use (Spring AI's
   `spring.ai.model.chat` property, or a `@Qualifier`, depending on the Spring AI version).
3. `ChatClientConfig` itself needs no changes: it already builds `defaultOptions` from the
   **portable** `ChatOptions` interface, not an Ollama-specific type, so the same options logic
   works against any provider.
4. Set `CHAT_PROVIDER` accordingly and add that provider's own connection property (e.g.
   `OPENAI_API_KEY`) to `.env.example` and `application.yml`.

## Logs

Every request is tagged with a correlation id (see `CorrelationIdFilter` in `common-lib`),
visible in:
- The `X-Correlation-Id` response header (and request header, if you send one yourself).
- Every log line for that request, in both the console and
  `backend/employee-support-app/logs/employee-support-app.log` (rotates daily / at 20MB,
  14 files kept).
