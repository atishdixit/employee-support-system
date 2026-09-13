# Infrastructure

## Docker

```bash
cd infra
docker compose up --build
```

This builds and runs two containers:

| Service    | Built from                     | Exposed at (default)     |
|------------|----------------------------------|----------------------------|
| `backend`  | `infra/backend.Dockerfile`      | `http://localhost:8080`   |
| `frontend` | `infra/frontend.Dockerfile`     | `http://localhost:4200`   |

**Ollama is not containerized.** It's expected to already be running on your host machine
(same as the non-Docker setup). The backend container reaches it via
`http://host.docker.internal:11434`, wired through `extra_hosts: host.docker.internal:host-gateway`
in `docker-compose.yml` — this works on Docker Desktop (Windows/Mac) and modern Docker Engine
on Linux.

### Backend image

Multi-stage build: `maven:3.9-eclipse-temurin-21` compiles the reactor (`common-lib` +
`employee-support-app`) and produces the executable jar; the runtime stage is a slim
`eclipse-temurin:21-jre` image that only contains that jar. Logs are written to `/app/logs`
inside the container, mounted to `../logs` on the host via a volume so they survive container
restarts and are inspectable without `docker exec`.

### Frontend image

Multi-stage build: `node:20-slim` runs `ng build --configuration production`; the runtime
stage is `nginx:alpine` serving the compiled static files from
`dist/frontend/browser`. `infra/nginx.conf` proxies `/api/*` to the `backend` service (Docker's
internal DNS resolves the service name), so the Angular app's production environment
(`environment.prod.ts`) uses a relative `/api/v1` base URL rather than a hardcoded host/port.

### Environment variables in Docker

`docker-compose.yml` reads the same root `.env` file (copy `.env.example` if you haven't) via
its default variable substitution (`${OLLAMA_MODEL:-llama3.2:3b}` etc.) — no separate Docker-only
configuration to maintain.

### Ports

Override `API_PORT` / `UI_PORT` in `.env` if 8080/4200 are already taken on your machine;
`docker-compose.yml` maps them straight through.

## Stopping

```bash
docker compose down
```

Add `-v` if you also want to drop the `../logs` bind mount's contents (it won't delete host
files, but detaches the volume definition).
