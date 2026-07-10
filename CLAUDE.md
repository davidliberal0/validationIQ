# ValidationIQ — Project Memory

Full spec: `ValidationIQ_MVP_Specification.md` (repo root). Read that first for
scope, data model, routes, and business rules — this file tracks build
progress and conventions, not the product spec itself.

## Status: Phase 1 (Setup) complete

Of the spec's 7-phase build order (section 21), only **Phase 1: Setup** is
done. Nothing beyond scaffolding exists yet — no entities, controllers,
services, repositories, or templates.

## What's completed

- Git repo initialized, remote `origin` → `git@github.com:davidliberal0/validationIQ.git`, branch `main`.
- Spring Boot project generated via Spring Initializr (not hand-written `pom.xml`):
  - **Spring Boot 3.5.16**, Java 21, Maven, jar packaging (deliberately pinned to 3.5.x over
    Initializr's newer 4.1.0 default — better tutorial/ecosystem support, matches spec's
    "understandable for an early-career developer" goal). If bumping Boot version later,
    treat it as a deliberate decision, not an incidental upgrade.
  - Dependencies: `web`, `data-jpa`, `validation`, `thymeleaf`, `postgresql` (runtime),
    `devtools` (runtime, optional), `lombok` (optional). Test stack (`spring-boot-starter-test`
    → JUnit 5 + Mockito + Spring Boot Test) came in by default.
  - Main class renamed from Initializr's auto-generated `ValidationiqApplication` to
    `ValidationIqApplication` (both `src/main` and `src/test`) to match spec section 13 exactly.
- Package skeleton created under `src/main/java/com/example/validationiq/`:
  `controller/`, `dto/`, `entity/`, `enums/`, `exception/`, `repository/`, `service/`
  — all currently empty (hold only `.gitkeep`), per spec section 13.
- Resource skeleton: `src/main/resources/static/{css,js}/` and
  `src/main/resources/templates/{fragments,projects,test-runs,failures}/` — also empty
  (`.gitkeep` placeholders). `dashboard.html` / `error.html` deliberately **not** created yet —
  they're real feature work for later phases, not stubs to scaffold now.
- `src/main/resources/application.properties` — datasource config reads
  `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` env vars with local defaults matching
  `docker-compose.yml` (`jdbc:postgresql://localhost:5432/validationiq`, user/pass `validationiq`).
  `spring.jpa.hibernate.ddl-auto=update` — dev-only convenience (no migration tool in scope
  per spec), revisit if this ever needs to be tightened for anything beyond local dev.
- `docker-compose.yml` — **Postgres only** (`postgres:16`, healthcheck, named volume
  `validationiq-pgdata`). No `app` service yet — that's explicitly Phase 7 ("Build the
  application Docker image") once there's real logic worth containerizing. Until then, run
  the app on the host (`./mvnw spring-boot:run`) against the containerized DB for fast
  DevTools reload.
- `.env` (git-ignored, real local values) + `.env.example` (committed, placeholder values) +
  `.gitignore` extended to exclude `.env`/`*.env.local`/`.DS_Store`. Verified via
  `git check-ignore -v .env` before any files were staged.
- Verified end-to-end: `docker compose up -d postgres` → healthy; `./mvnw spring-boot:run` →
  `Started ValidationIqApplication` with Hikari successfully connecting to the real Postgres
  container (not a stub); `curl localhost:8080/` → HTTP 404 (correct — no controllers exist
  yet, this just proves Tomcat is serving); `./mvnw test` → 1/1 passing (Spring context +
  datasource bean load cleanly). This satisfies spec section 16's "basic application context
  test" requirement.
- Two commits on `main`, both pushed to `origin`:
  1. `eb53ebb` — initial scaffold (pom.xml, package structure, application.properties) — made
     directly by the user, not by Claude.
  2. `fabb796` — `docker-compose.yml`, `.env.example`, `.gitignore` additions — made by Claude
     at the user's explicit request.

## Conventions established this session

- **Plan mode for anything non-trivial.** User wants an explicit plan reviewed and approved
  before implementation starts on meaningful work — don't skip straight to coding.
- **Explain reasoning as you go, not just what was done.** User explicitly asked for the "why"
  behind actions to be stated during the work, not only summarized after.
- **Never commit or push without being asked.** Confirmed twice this session — always stop and
  let the user commit themselves, or wait for an explicit "commit and push" instruction. When
  asked, stage an explicit file list (not `git add -A`) so nothing unintended (e.g. a real
  `.env`) rides along.
- **Stay strictly within the current phase's scope.** Don't create stub files, empty
  controllers, or placeholder views for future phases "to save time later" — the spec (section
  22) explicitly wants one feature built at a time, and premature scaffolding just becomes
  dead weight to rewrite.
- **Match the spec's exact naming/paths when it's explicit** (e.g. `ValidationIqApplication.java`,
  the section 13 package layout) even when a generator (Initializr) defaults to something close
  but not identical.
- Java/Maven/Docker are all available locally (Java 21.0.11, Maven 3.9.12, Docker 29.1.3 +
  Compose 2.40.3) — no environment setup needed in future sessions.

## Next: Phase 2 — Domain Model

Per spec section 21, in order:
1. Enums: `TestRunResult` (PASS/FAIL/PARTIAL), `FailureSeverity` (LOW/MEDIUM/HIGH/CRITICAL),
   `FailureStatus` (OPEN/IN_PROGRESS/RESOLVED/CLOSED) — see spec section 8 for exact values.
2. `Project` entity (spec 7.1) — id, name, description, createdAt, updatedAt; one-to-many to TestRun.
3. `TestRun` entity (spec 7.2) — includes `projectId`, `result` enum, `executionDate`;
   many-to-one to Project, one-to-many to Failure.
4. `Failure` entity (spec 7.3) — includes `testRunId`, `severity`/`status` enums,
   `expectedResult`/`actualResult`; many-to-one to TestRun, one-to-many to Comment.
5. `Comment` entity (spec 7.4) — `authorName`, `content`, `failureId`; many-to-one to Failure.
6. Repositories (Spring Data JPA) for each entity.
7. Verify tables are created correctly (`ddl-auto=update` will auto-create them — confirm via
   `psql` or a DB client against the running `validationiq-postgres` container).

Business rules to keep in mind while building entities (spec section 11): every TestRun needs
a Project, every Failure needs a TestRun, every Comment needs a Failure; deleting a Failure
should cascade-delete its Comments; deleting a Project/TestRun with children should **not**
cascade silently — that needs deliberate handling (likely in the service layer, not just JPA
cascade annotations) in a later phase.

## How to run / verify locally

```bash
# start Postgres
docker compose up -d postgres

# run the app (reads .env-equivalent defaults baked into application.properties)
./mvnw spring-boot:run

# run tests
./mvnw test

# stop
docker compose down   # add -v only if you intentionally want to wipe the DB volume
```
