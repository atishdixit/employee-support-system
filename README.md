# Employee Support System

An AI assistant that answers employee questions about company policy — **Leave, Holidays,
Maternity/Paternity, Promotion, and Office Work Hours** — backed by a **locally-running Ollama
model**. No cloud API keys, no database: five seeded employees, one stuffed system prompt, and
a layered Spring Boot + Angular application built around it.

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the full design, [docs/SETUP.md](docs/SETUP.md)
for environment variables and profiles, and [docs/INFRASTRUCTURE.md](docs/INFRASTRUCTURE.md) for
Docker.

## Features → where they live

| Requirement                                   | Where                                                                 |
|------------------------------------------------|------------------------------------------------------------------------|
| Local Ollama model                             | `spring-ai-starter-model-ollama`, configured in `application.yml`     |
| Prompt stuffing                                | `promptTemplates/companyPolicySystemPrompt.st` (all 5 policy areas)   |
| Roles (System / User / Assistant)              | `PolicyAssistantService` builds the system+user turn per request       |
| Advisors — default, logging, token audit       | `TokenUsageAuditAdvisor`, `RequestTraceAdvisor` (always-on defaults)   |
| Advisors — per-request interceptor             | `DebugEchoAdvisor` (only attached when `?debug=true`)                  |
| Safeguard for out-of-scope questions           | `TopicGuardrailAdvisor` (keyword allowlist, short-circuits before the model is called) |
| Portable, model-agnostic ChatOptions           | `ChatClientConfig` builds options via `ChatOptions.builder()` (no OpenAI/Ollama-specific type) |
| Bean-based structured response                 | `PolicyChatResponse` / `ModelAnswer` records, via `.call().responseEntity(...)` |
| 5 employees, no database                       | `EmployeeDirectoryService` (in-memory seed list)                       |
| No RAG, only a small prompt                    | One consolidated `.st` template, no vector store                       |
| Configurable max/min tokens                    | `ChatModelProperties` (`app.chat.max-tokens` / `min-tokens`), fails fast at startup if max < min |
| Common code as a reusable library (jar)        | `backend/common-lib` (exceptions, correlation-id filter, Log4j2-based logging) |
| Log4j2, file logging, correlation id per request | `common-lib`'s `CorrelationIdFilter` + `log4j2.xml` (rolling file appender) |
| Configurable model/provider                    | `app.chat.*` + `spring.ai.ollama.*` properties; see "Adding a new LLM provider" in SETUP.md |
| .env + profiles (dev/test/stage/prod)          | `spring-dotenv` + `application-{profile}.yml`                          |
| Docker support                                 | `infra/backend.Dockerfile`, `infra/frontend.Dockerfile`, `infra/docker-compose.yml` |
| Angular, component-based, segregated layers    | `frontend/src/app/{core,features,shared}`                              |
| Swagger / OpenAPI docs                         | `springdoc-openapi`, `OpenApiConfig` — `/swagger-ui/index.html`        |
| JWT authentication                             | `SecurityConfig`, `JwtAuthenticationFilter` (app) + `JwtTokenProvider` (common-lib) |
| 5 hardcoded demo users                         | `DemoUserService` — one login per seeded employee, BCrypt-hashed passwords |
| Employee identity from the token, not the client | `PolicyChatController`/`EmployeeController` read `authentication.getName()`, not a client-supplied id |
| High-level component diagram                   | [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md#high-level-component-diagram) |

## Quick start (Windows, one click)

```bash
setup.bat
```

Builds the backend (Maven reactor), installs frontend dependencies, and pulls the configured
Ollama model if it's missing. Run once.

```bash
run.bat
```

Starts Ollama (if not already running), the Spring Boot backend on `http://localhost:8080`,
and the Angular frontend on `http://localhost:4200`, then opens your browser. Log in with any
of the 5 demo accounts (`e001`-`e005`, password `Passw0rd!` — shown on the login screen too).
API docs: `http://localhost:8080/swagger-ui/index.html`.

## Manual start

```bash
cd backend
mvn install -DskipTests
java -jar employee-support-app/target/employee-support-app.jar --spring.profiles.active=dev

# in a second terminal
cd frontend
npm install
npx ng serve
```

## Docker

```bash
cd infra
docker compose up --build
```

See [docs/INFRASTRUCTURE.md](docs/INFRASTRUCTURE.md) for details (Ollama stays on your host
machine; only the backend and frontend run in containers).

## Project layout

```
backend/
  common-lib/                 Reusable jar: exceptions, correlation-id filter, JWT mechanics
  employee-support-app/       The Spring Boot application (com.ext.emp.support)
                               includes security/ (JWT filter, demo users, Spring Security config)
frontend/                     Angular 18, standalone components
infra/                        Dockerfiles + docker-compose.yml + nginx.conf
docs/                         ARCHITECTURE.md, SETUP.md, INFRASTRUCTURE.md
.env.example                  Copy to .env and adjust
setup.bat / run.bat           One-time install / one-click start
```

## A note on speed and answer quality

Every question is a real call to your local Ollama model, run on your own CPU/GPU. On
CPU-only inference (no dedicated GPU), one answer can take 20-90+ seconds — that's expected.
The default model (`llama3.2:3b`) is small and fast to run locally but occasionally produces
terser answers than a larger model would; swap `OLLAMA_MODEL` in `.env` for a bigger
instruction-tuned model if you have the hardware for it.
