# Architecture

## Layers

```
┌─────────────────────────────────────────────────────────────────┐
│ Angular frontend (frontend/)                                    │
│   features/ (login, policy-chat) → core/services/                │
│   → HttpClient (+ auth + correlation-id interceptors)             │
└───────────────────────────────┬───────────────────────────────────┘
                                 │ HTTP (JSON), Authorization: Bearer <JWT>
┌───────────────────────────────▼───────────────────────────────────┐
│ employee-support-app (backend/employee-support-app)              │
│                                                                   │
│  controller/   AuthController, PolicyChatController,             │
│                EmployeeController                                │
│  security/     SecurityConfig, JwtAuthenticationFilter,           │
│                DemoUserService, Rest{AuthenticationEntryPoint,   │
│                AccessDeniedHandler}                               │
│  service/      PolicyAssistantService, EmployeeDirectoryService  │
│  advisor/      TopicGuardrailAdvisor, TokenUsageAuditAdvisor,     │
│                RequestTraceAdvisor (defaults) + DebugEchoAdvisor  │
│                (per-request only)                                │
│  config/       ChatClientConfig, ChatModelProperties,             │
│                JwtProperties, OpenApiConfig (Swagger)             │
│  model/        Employee, PolicyChatRequest/Response, ModelAnswer,│
│                LoginRequest/Response                              │
│                                                                   │
│  depends on ↓                                                    │
├───────────────────────────────────────────────────────────────────┤
│ common-lib (backend/common-lib) - reusable jar                   │
│   exception/  ApplicationException, ErrorCode, ErrorResponse,     │
│               GlobalExceptionHandler (@RestControllerAdvice)      │
│   logging/    CorrelationIdFilter, CorrelationIdConstants         │
│   security/   JwtTokenProvider, JwtAuthenticationToken            │
│               (generic JWT mechanics, no "Employee" knowledge)    │
└───────────────────────────────┬───────────────────────────────────┘
                                 │ spring-ai-starter-model-ollama
┌───────────────────────────────▼───────────────────────────────────┐
│ Ollama (runs on the host machine, not in a container)             │
└─────────────────────────────────────────────────────────────────┘
```

`common-lib` is a separate Maven module (packaging `jar`) so its exception handling,
correlation-id/logging, and now JWT mechanics could be reused by a second Spring Boot service
later without copy-pasting code — it depends on nothing from `employee-support-app`. Spring
Security and JJWT are declared `<optional>true</optional>` there, so a future consumer that
doesn't need auth isn't forced to pull them in.

## High-level component diagram

```mermaid
graph TB
    subgraph Angular["Angular Frontend"]
        Login["LoginComponent"]
        Chat["PolicyChatComponent<br/>+ chat-input / chat-message-list"]
        AuthSvc["AuthService"]
        ChatSvc["PolicyChatService"]
        Interceptors["authInterceptor +<br/>correlationIdInterceptor"]
    end

    subgraph SwaggerUI["Swagger UI (dev-time)"]
        Swagger["/swagger-ui/index.html"]
    end

    subgraph Security["Spring Security filter chain"]
        JwtFilter["JwtAuthenticationFilter"]
        CorrFilter["CorrelationIdFilter"]
    end

    subgraph Controllers["Controllers"]
        AuthCtrl["AuthController<br/>/api/v1/auth/login"]
        PolicyCtrl["PolicyChatController<br/>/api/v1/policy/chat"]
        EmpCtrl["EmployeeController<br/>/api/v1/employees, /me"]
    end

    subgraph Services["Services"]
        DemoUsers["DemoUserService<br/>(5 hardcoded accounts)"]
        PolicySvc["PolicyAssistantService"]
        EmpSvc["EmployeeDirectoryService<br/>(5 in-memory employees)"]
    end

    subgraph Advisors["ChatClient advisor chain"]
        Guardrail["TopicGuardrailAdvisor"]
        Trace["RequestTraceAdvisor"]
        TokenAudit["TokenUsageAuditAdvisor"]
        Debug["DebugEchoAdvisor (per-request)"]
    end

    subgraph Foundation["common-lib (shared jar)"]
        JwtProvider["JwtTokenProvider"]
        ErrorHandling["GlobalExceptionHandler /<br/>ErrorResponse"]
    end

    Ollama[("Ollama<br/>llama3.2:3b")]

    Login --> AuthSvc
    Chat --> ChatSvc
    AuthSvc --> Interceptors
    ChatSvc --> Interceptors
    Interceptors -->|HTTP| CorrFilter --> JwtFilter
    JwtFilter --> AuthCtrl
    JwtFilter --> PolicyCtrl
    JwtFilter --> EmpCtrl
    Swagger -.->|try-it-out, same JWT| AuthCtrl

    AuthCtrl --> DemoUsers
    AuthCtrl --> EmpSvc
    PolicyCtrl --> PolicySvc
    EmpCtrl --> EmpSvc
    PolicySvc --> EmpSvc
    PolicySvc --> Guardrail --> Trace --> TokenAudit --> Ollama

    AuthCtrl --> JwtProvider
    JwtFilter --> JwtProvider
    Controllers -.-> ErrorHandling
```

## Request flow (one policy question)

```mermaid
sequenceDiagram
    participant UI as Angular UI
    participant CorrFilter as CorrelationIdFilter (common-lib)
    participant JwtFilter as JwtAuthenticationFilter
    participant Ctrl as PolicyChatController
    participant Svc as PolicyAssistantService
    participant Guard as TopicGuardrailAdvisor
    participant Trace as RequestTraceAdvisor
    participant Token as TokenUsageAuditAdvisor
    participant Ollama as Ollama (llama3.2:3b)

    UI->>CorrFilter: POST /api/v1/policy/chat {question}<br/>Authorization: Bearer <JWT>
    CorrFilter->>CorrFilter: read/generate X-Correlation-Id, put in Log4j2 ThreadContext
    CorrFilter->>JwtFilter: forward request
    JwtFilter->>JwtFilter: validate JWT, set Authentication (principal = employeeId)
    JwtFilter->>Ctrl: forward request (or 401 via RestAuthenticationEntryPoint if invalid/missing)
    Ctrl->>Svc: ask(authentication.getName(), question, debug)
    Svc->>Svc: look up Employee, stuff system prompt template with policy + employee context
    Svc->>Guard: chatClient.prompt()...call() (guardrail runs first, order=0)
    alt question is off-topic
        Guard-->>Svc: synthetic ModelAnswer(OUT_OF_SCOPE) - Ollama never called
    else question is in-scope
        Guard->>Trace: chain.nextCall(...)
        Trace->>Token: chain.nextCall(...)
        Token->>Ollama: chain.nextCall(...) - the real model call
        Ollama-->>Token: ChatResponse (JSON matching ModelAnswer schema)
        Token-->>Trace: logs token usage
        Trace-->>Guard: logs elapsed time
        Guard-->>Svc: ChatClientResponse
    end
    Svc->>Svc: assemble PolicyChatResponse (adds correlationId, tokenUsage)
    Svc-->>Ctrl: PolicyChatResponse
    Ctrl-->>CorrFilter: 200 OK + X-Correlation-Id header
    CorrFilter-->>UI: response (interceptor remembers the id for the next call)
```

Key points:
- **The employee identity comes from the verified JWT, never from the request body.** Before
  auth was added, `PolicyChatRequest` carried a client-supplied `employeeId` that was trusted
  as-is; now `PolicyChatController` reads `authentication.getName()` (the JWT's subject, set by
  `JwtAuthenticationFilter`), so a caller can only ever ask questions as themselves.
- **Guardrail runs first** (`getOrder() == 0`) and can short-circuit the whole chain — no Ollama
  call, no token cost, near-instant response — before `RequestTraceAdvisor` or
  `TokenUsageAuditAdvisor` even see the request.
- **DebugEchoAdvisor** is not in this diagram because it's not a default: `PolicyAssistantService`
  only adds it to the per-request advisor list when the controller is called with `?debug=true`.
- **Structured output**: the model is only ever asked to produce `ModelAnswer` (`answer`,
  `category`) — never asked to invent `correlationId` or `tokenUsage`, which
  `PolicyAssistantService` fills in itself from request-scoped state.
- **Consistent error shape everywhere**: a missing/invalid JWT (401, via
  `RestAuthenticationEntryPoint`) and a business exception (via `GlobalExceptionHandler`) both
  return the same `ErrorResponse` JSON, correlation id included.

## Configuration model

- `ChatModelProperties` (`app.chat.*`) owns the app-level knobs: `model`, `temperature`,
  `max-tokens`, `min-tokens`. It validates `max-tokens >= min-tokens` at startup
  (`@PostConstruct`), so a bad `.env` fails fast instead of silently misbehaving.
- `JwtProperties` (`app.security.jwt.*`) owns the signing secret and token lifetime, validated
  the same way (secret must be present and ≥256 bits for HS256).
- `spring.ai.ollama.*` owns the actual model connection (base URL, model name) — driven by the
  same `OLLAMA_BASE_URL` / `OLLAMA_MODEL` environment variables so there's one source of truth.
- `ChatClientConfig` combines both into the shared `ChatClient` bean's `defaultOptions` (built
  via the **portable** `org.springframework.ai.chat.prompt.ChatOptions` builder — no
  Ollama-specific or OpenAI-specific type is referenced), and its `defaultAdvisors`.

## Why no RAG / no database

The five policy areas fit comfortably in one system prompt (~500 words) and the "employee
directory" is five records — a vector store or SQL database would add operational complexity
with no benefit at this scale. `EmployeeDirectoryService` and the `.st` template are the two
places to swap in a database and a retrieval step respectively, if the scope grows. The same
applies to `DemoUserService`: it's a hardcoded stand-in for a real user store, isolated behind
one small interface-shaped class so swapping it for a database-backed one later touches nothing
else (`SecurityConfig`, `JwtAuthenticationFilter`, and `JwtTokenProvider` don't know it exists).
