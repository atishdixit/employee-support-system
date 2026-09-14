# API Use Cases

All request/response examples below are real shapes taken directly from the code
(`backend/employee-support-app/src/main/java/com/ext/emp/support/model/*`, `controller/*`) —
not illustrative pseudo-JSON. Try any of them live via Swagger UI
(`http://localhost:8080/swagger-ui/index.html`) or `curl`; see [SETUP.md](SETUP.md#demo-logins)
for the 5 demo accounts.

Base URL: `http://localhost:8080` (or `API_PORT` from `.env`). All endpoints are versioned
under `/api/v1`.

| # | Use case | Method & Endpoint | Auth required |
|---|----------|--------------------|-----------------|
| 1 | [Log in](#1-log-in) | `POST /api/v1/auth/login` | No |
| 2 | [Login with wrong credentials](#2-login-with-wrong-credentials) | `POST /api/v1/auth/login` | No |
| 3 | [Ask an in-scope policy question](#3-ask-an-in-scope-policy-question) | `POST /api/v1/policy/chat` | Yes |
| 4 | [Ask an out-of-scope question (guardrail)](#4-ask-an-out-of-scope-question-guardrail) | `POST /api/v1/policy/chat` | Yes |
| 5 | [Ask with debug mode on](#5-ask-with-debug-mode-on) | `POST /api/v1/policy/chat?debug=true` | Yes |
| 6 | [Call a protected endpoint with no/invalid token](#6-call-a-protected-endpoint-with-noinvalid-token) | any `/api/v1/**` except `/auth/login` | Yes |
| 7 | [Ask a question with a blank body](#7-ask-a-question-with-a-blank-body) | `POST /api/v1/policy/chat` | Yes |
| 8 | [List all employees](#8-list-all-employees) | `GET /api/v1/employees` | Yes |
| 9 | [Look up the logged-in employee](#9-look-up-the-logged-in-employee) | `GET /api/v1/employees/me` | Yes |

---

## 1. Log in

**Purpose:** Exchange a demo username/password for a JWT. This is the only endpoint that
doesn't require a token — everything else needs the `Authorization` header this returns.

`POST /api/v1/auth/login`

**Request body:**
```json
{
  "username": "e001",
  "password": "Passw0rd!"
}
```

**Response — `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJFMDAxIiwibmFtZSI6IkFzaGEgVmVybWEiLCJpYXQiOjE3ODkzMDY4MDAsImV4cCI6MTc4OTMxMDQwMH0.aGHx06W9i0eYd8qXD0_ZN_PRBnGBT2K0vDnE3bBnNDegfAQZ14xzXtExihuiU295",
  "expiresInSeconds": 3600,
  "employee": {
    "id": "E001",
    "name": "Asha Verma",
    "department": "Engineering",
    "designation": "Senior Software Engineer",
    "joiningDate": "2021-06-14"
  }
}
```

`expiresInSeconds` mirrors `app.security.jwt.expiration-minutes` (default 60 minutes = 3600
seconds). The token's subject is the employee id (`E001`) — this is what every other endpoint
trusts instead of anything the client sends.

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"e001","password":"Passw0rd!"}'
```

---

## 2. Login with wrong credentials

**Purpose:** Confirm bad credentials are rejected without leaking whether the username or the
password was wrong (both cases return the same generic message).

`POST /api/v1/auth/login`

**Request body:**
```json
{ "username": "e001", "password": "wrong-password" }
```

**Response — `401 Unauthorized`:**
```json
{
  "timestamp": "2026-09-13T13:40:14.031839300Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid username or password",
  "path": "/api/v1/auth/login",
  "correlationId": "52393dc2-b1c9-4d42-8d8a-10b14c643e89"
}
```

This is the same `ErrorResponse` shape (`common-lib`'s `GlobalExceptionHandler`) used for every
error in the API, so client-side error handling only needs to understand one format.

---

## 3. Ask an in-scope policy question

**Purpose:** The core feature — ask about Leave, Holidays, Maternity/Paternity, Promotion, or
Office Work Hours and get an answer grounded in the stuffed policy prompt, personalized with
the logged-in employee's context (tenure, department, etc.).

`POST /api/v1/policy/chat`

**Headers:** `Authorization: Bearer <token from step 1>`

**Request body:**
```json
{ "question": "How many days of paternity leave do fathers get?" }
```

Note there is no `employeeId` field — the asking employee comes from the JWT
(`authentication.getName()` in `PolicyChatController`), never from the request body.

**Response — `200 OK`:**
```json
{
  "answer": "2 weeks fully paid",
  "category": "MATERNITY_PATERNITY",
  "employeeContext": "Asha Verma (Senior Software Engineer, Engineering)",
  "correlationId": "b5335558-621a-4a77-959d-fae11afb5164",
  "tokenUsage": {
    "promptTokens": 917,
    "completionTokens": 28,
    "totalTokens": 945
  }
}
```

`category` is one of `LEAVE`, `HOLIDAY`, `MATERNITY_PATERNITY`, `PROMOTION`, `WORK_HOURS`,
`GENERAL`, or `OUT_OF_SCOPE` — the model classifies its own answer as part of the structured
output (`ModelAnswer` bean). This call is a real Ollama inference, so on CPU-only hardware it
can take anywhere from ~20 to 90+ seconds.

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/policy/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token>" \
  -d '{"question":"How many days of paternity leave do fathers get?"}'
```

---

## 4. Ask an out-of-scope question (guardrail)

**Purpose:** Demonstrate the safeguard that keeps the assistant on-topic. `TopicGuardrailAdvisor`
checks the question against an allowed-keyword list *before* calling Ollama at all — if nothing
matches, it returns a canned refusal instantly, with **zero token cost**.

`POST /api/v1/policy/chat`

**Headers:** `Authorization: Bearer <token>`

**Request body:**
```json
{ "question": "What is the capital of Japan?" }
```

**Response — `200 OK`** (note: still a 200, not an error — this is a normal, successful
classification, just an out-of-scope one):
```json
{
  "answer": "I can only help with questions about company Leave, Holiday, Maternity/Paternity, Promotion, and Office Work Hours policies. Could you rephrase your question around one of those topics?",
  "category": "OUT_OF_SCOPE",
  "employeeContext": "Priya Nair (HR Business Partner, Human Resources)",
  "correlationId": "d1f660f2-d175-439b-ae7b-b4680d12d52e",
  "tokenUsage": { "promptTokens": 0, "completionTokens": 0, "totalTokens": 0 }
}
```

`tokenUsage` is all zeros because Ollama was never called — you can confirm this yourself by
checking `logs/employee-support-app.log` for that `correlationId`: only a
`TopicGuardrailAdvisor` warning line appears, no `RequestTraceAdvisor` / `TokenUsageAuditAdvisor`
lines (those only run if the guardrail lets the request through).

---

## 5. Ask with debug mode on

**Purpose:** Demonstrate a **per-request** advisor (as opposed to the three defaults that run on
every call) — `DebugEchoAdvisor` is only attached when the caller passes `?debug=true`, and logs
the full untruncated prompt and raw model output for troubleshooting.

`POST /api/v1/policy/chat?debug=true`

**Headers:** `Authorization: Bearer <token>`

**Request body:**
```json
{ "question": "How many public holidays do we get?" }
```

**Response:** identical shape to [use case 3](#3-ask-an-in-scope-policy-question). The
difference is only visible in the log file, which gains two extra `[debug]`-prefixed lines
(`DebugEchoAdvisor`) not present for the same question without `?debug=true`.

---

## 6. Call a protected endpoint with no/invalid token

**Purpose:** Show what every endpoint under `/api/v1/**` (except `/auth/login`) does when the
`Authorization` header is missing, malformed, or carries an expired/invalid JWT.

`GET /api/v1/employees` (no `Authorization` header at all)

**Response — `401 Unauthorized`:**
```json
{
  "timestamp": "2026-09-13T13:40:00.918158Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "A valid Authorization: Bearer <token> header is required",
  "path": "/api/v1/employees",
  "correlationId": "3d6228d9-e710-4591-8844-d1237995749b"
}
```

This is produced by `RestAuthenticationEntryPoint` — the request never reaches
`EmployeeController` at all; `JwtAuthenticationFilter` and Spring Security's authorization rule
reject it first.

The same thing happens for a **present but malformed/invalid** token (garbage string, expired,
wrong signature) — `JwtTokenProvider.parseClaims(...)` catches the parse failure and returns
nothing, so the filter simply leaves the request unauthenticated and the identical 401 above is
returned:

```bash
curl http://localhost:8080/api/v1/employees -H "Authorization: Bearer garbage.not.a.jwt"
```
```json
{
  "timestamp": "2026-09-14T05:39:07.092658500Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "A valid Authorization: Bearer <token> header is required",
  "path": "/api/v1/employees",
  "correlationId": "3c9c6485-e0ee-4883-99f9-0cddf89fe053"
}
```

---

## 7. Ask a question with a blank body

**Purpose:** Show request validation independent of authentication — `PolicyChatRequest.question`
is `@NotBlank`, enforced before `PolicyAssistantService` (and therefore Ollama) is ever invoked.

`POST /api/v1/policy/chat`

**Headers:** `Authorization: Bearer <token>`

**Request body:**
```json
{ "question": "" }
```

**Response — `400 Bad Request`** (`GlobalExceptionHandler` has a dedicated handler for
`MethodArgumentNotValidException` that surfaces the real `@NotBlank` message, in the same
`ErrorResponse` shape as every other error in the app):
```json
{
  "timestamp": "2026-09-14T05:42:26.252584600Z",
  "status": 400,
  "error": "Bad Request",
  "message": "question must not be blank",
  "path": "/api/v1/policy/chat",
  "correlationId": "671efb3f-fd2d-4d92-95a1-9ec82fb3608b"
}
```

---

## 8. List all employees

**Purpose:** Enumerate the 5 seeded, in-memory employee records — useful for admin-style
tooling or just exploring the demo data (there is no database; see `EmployeeDirectoryService`).

`GET /api/v1/employees`

**Headers:** `Authorization: Bearer <token>`

**Response — `200 OK`:**
```json
[
  { "id": "E001", "name": "Asha Verma",  "department": "Engineering",     "designation": "Senior Software Engineer", "joiningDate": "2021-06-14" },
  { "id": "E002", "name": "Rohan Mehta", "department": "Sales",           "designation": "Account Executive",        "joiningDate": "2023-01-09" },
  { "id": "E003", "name": "Priya Nair",  "department": "Human Resources", "designation": "HR Business Partner",      "joiningDate": "2019-03-02" },
  { "id": "E004", "name": "Karan Singh", "department": "Engineering",     "designation": "Engineering Manager",      "joiningDate": "2018-11-20" },
  { "id": "E005", "name": "Meera Iyer",  "department": "Finance",         "designation": "Financial Analyst",        "joiningDate": "2022-08-30" }
]
```

---

## 9. Look up the logged-in employee

**Purpose:** Let the frontend resolve "who am I?" from just the JWT, without re-sending
credentials — used by the Angular app after login (and on page reload, since the token
persists in `sessionStorage` but the full employee profile doesn't need to be re-typed).

`GET /api/v1/employees/me`

**Headers:** `Authorization: Bearer <token for e002>`

**Response — `200 OK`:**
```json
{
  "id": "E002",
  "name": "Rohan Mehta",
  "department": "Sales",
  "designation": "Account Executive",
  "joiningDate": "2023-01-09"
}
```

The returned employee always matches whoever the token belongs to — there is no way to pass a
different employee id and see someone else's record (this endpoint ignores everything except
the token).

---

## Cross-cutting behaviour (applies to every use case above)

- **Correlation id**: every response carries an `X-Correlation-Id` header and includes the same
  id in its JSON body (`correlationId` field, or inside an `ErrorResponse`). Send your own
  `X-Correlation-Id` request header to have it echoed back instead of a generated one — useful
  for tracing a single logical operation across multiple calls in `logs/employee-support-app.log`.
- **Swagger UI**: every endpoint above is listed at `/swagger-ui/index.html` with a live
  "Try it out" button; click **Authorize** once with a token from use case 1 to unlock the
  protected ones for the rest of the session.
