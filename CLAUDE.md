# ValidationIQ — Project Memory

Full spec: `ValidationIQ_MVP_Specification.md` (repo root). Read that first for
scope, data model, routes, and business rules — this file tracks build
progress and conventions, not the product spec itself.

## Status: Phase 2 (Domain Model) complete

Of the spec's 7-phase build order (section 21), **Phase 1: Setup** and
**Phase 2: Domain Model** are done. No controllers, services, or templates
exist yet — that starts with Phase 3.

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

## Phase 2: Domain Model (this session)

- **Enums** (`enums/`): `TestRunResult` (PASS/FAIL/PARTIAL), `FailureSeverity`
  (LOW/MEDIUM/HIGH/CRITICAL), `FailureStatus` (OPEN/IN_PROGRESS/RESOLVED/CLOSED)
  — plain Java enums, stored via `@Enumerated(EnumType.STRING)` wherever
  referenced (readable DB values, safe against reordering — vs. fragile
  integer ordinals).
- **Entities** (`entity/`): `Project`, `TestRun`, `Failure`, `Comment`, fields
  exactly matching spec sections 7.1–7.4. Design decisions made explicitly
  with the user (who is new to backend development — favored visible/explicit
  code over framework "magic" throughout):
  - **Unidirectional relationships only.** Child entities hold a `@ManyToOne`
    reference to their parent (`TestRun.project`, `Failure.testRun`,
    `Comment.failure`); parents do **not** hold `@OneToMany` collections back
    to children. To list a project's test runs, query the repository (e.g.
    `testRunRepository.findByProjectId(id)`) rather than `project.getTestRuns()`.
    No such finder methods exist yet — add them in whichever later phase
    first needs them, not speculatively now.
  - **No Lombok** — all getters/setters are hand-written, even though
    `lombok` remains an unused optional dependency in `pom.xml` from the
    Initializr default (left alone; removing it is out of this phase's scope).
  - **`@PrePersist`/`@PreUpdate`** lifecycle callback methods (not Spring Data
    JPA auditing) set `createdAt`/`updatedAt` directly in each entity.
    `Comment` only has `createdAt` (no `updatedAt`), matching spec 7.4.
  - `Failure.status` defaults to `OPEN` and `Failure.severity` defaults to
    `MEDIUM` via field initializers, per business rules 4–5 (spec section 11)
    — set at the entity level so any code path that creates a `Failure`
    without explicitly setting these gets the correct default, ahead of the
    form-layer defaults referenced in spec 9.9.
  - No Bean Validation annotations (`@NotBlank`, `@Size`) on entities —
    spec section 14 recommends validating via DTOs/form objects instead,
    which arrive with the controllers in Phase 3+.
- **Repositories** (`repository/`): `ProjectRepository`, `TestRunRepository`,
  `FailureRepository`, `CommentRepository` — each just `extends
  JpaRepository<T, Long>`, no custom query methods yet.
- **Verified**:
  - `./mvnw compile` — clean.
  - `docker compose up -d postgres` + `./mvnw spring-boot:run` — Hibernate
    (`ddl-auto=update`) created `project`, `test_run`, `failure`, `comment`
    tables with correct columns, types, check constraints on the enum
    columns, and foreign keys (confirmed via `psql \dt` / `\d <table>`).
  - `./mvnw test` — 2/2 passing: the existing context test, plus a new
    `src/test/java/com/example/validationiq/repository/EntityRelationshipTest.java`
    (`@DataJpaTest` against the real Postgres container, since no embedded
    test DB is configured) that persists a full Project → TestRun → Failure →
    Comment chain and asserts every save/reload round-trip and FK association,
    including the `OPEN`/`MEDIUM` defaults — satisfies spec section 22
    instruction 12 ("verify entity relationships before building views")
    automatically rather than only via manual `psql` checks.
- Not yet committed — per the "never commit without being asked" convention
  below, the user is reviewing this work before deciding to commit/push.

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
- **Explain unfamiliar concepts in plain terms before presenting a technical decision.** The user
  is new to backend development — don't lead with jargon-laden options (e.g. "bidirectional vs.
  unidirectional JPA relationships"); walk through what each choice actually means and why it
  matters first, then ask.
- **Keep this file updated as work is completed, not just at session end.** The user wants to be
  able to start a fresh Claude Code session and immediately know where the project was left off —
  update "Status" / "What's completed" / "Next" incrementally as milestones land.
- Java/Maven/Docker are all available locally (Java 21.0.11, Maven 3.9.12, Docker 29.1.3 +
  Compose 2.40.3) — no environment setup needed in future sessions.

## Next: Phase 3 — Projects

Per spec section 21, in order:
1. `ProjectService` — create/update/delete logic; enforce business rule 9 (don't allow deleting a
   project that has test runs, unless cascade is deliberately implemented — spec section 9.2).
2. `ProjectController` — routes per spec section 12: `GET /projects`, `GET /projects/new`,
   `POST /projects`, `GET /projects/{id}`, `GET /projects/{id}/edit`, `POST /projects/{id}`,
   `POST /projects/{id}/delete`.
3. `ProjectForm` DTO (spec section 14) — `name` (`@NotBlank`, `@Size`), `description` (`@Size`) —
   this is where Bean Validation annotations belong, per the Phase 2 decision to keep them off entities.
4. Projects list template (spec 9.2): name, description, test run count, created date.
5. Create/edit form template (spec 9.3).
6. Project details template (spec 9.4): name, description, timestamps, associated test runs.
   Since `TestRun` doesn't hold a back-reference collection (Phase 2's unidirectional decision),
   this page needs `testRunRepository.findByProjectId(id)` — likely the first custom repository
   query method added in the project, via a service method.
7. Validation error display, per spec section 14.

Business rules to keep in mind (spec section 11): every TestRun needs a Project (already enforced
at the entity level via `nullable = false`); deleting a Project with TestRuns should **not**
cascade silently — needs deliberate handling in `ProjectService`, not just a JPA cascade annotation;
invalid project IDs must show a user-friendly error (`ResourceNotFoundException` +
`@ControllerAdvice`, spec section 15) rather than a raw 404/stack trace.

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
