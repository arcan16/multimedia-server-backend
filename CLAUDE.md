# CLAUDE.md — Backend

This file provides guidance to Claude Code (claude.ai/code) when working in `Backend/` (the git repository).

## Scope

The MultimediaServer REST API: Spring Boot 4.1 (Java 17, Maven) on MySQL with stateless JWT auth. It has full CRUD for users, genres, movies/series (with poster upload) and multimedia files (video upload + HTTP Range streaming). User-facing docs are in `README.md`.

The web client lives in `../Frontend/` and is its own context: **do not read or edit it from a backend session**. Its `CLAUDE.md` holds the API contract the client relies on. **Any change to endpoints, request/response shapes, status codes or auth must also update the "API contract" section of `../Frontend/CLAUDE.md`** (and `README.md`), so frontend sessions stay correct.

## Commands

Run from this folder. Use `mvnw.cmd` on Windows and `./mvnw` in Git Bash.

```bash
./mvnw spring-boot:run          # run the app (needs the env vars below)
./mvnw -q compile               # quick check after changes
./mvnw package                  # build the jar into target/
./mvnw test                     # run all tests
./mvnw test -Dtest=MultimediaServerApplicationTests#contextLoads   # run a single test
```

### Configuration

`application.properties` holds no secrets. Everything comes from the environment:

- `DB_URL_MULTIMEDIA`, `DB_USERNAME_MULTIMEDIA`, `DB_PASSWORD_MULTIMEDIA`: the MySQL connection (required).
- `jwt.secret.key`: a Base64-encoded HMAC key (at least 32 bytes decoded). `jwt.expiration.key`: the token lifetime in milliseconds. In the properties file these two placeholders refer to themselves, so they must be supplied externally, for example with the env vars `JWT_SECRET_KEY` / `JWT_EXPIRATION_KEY` (relaxed binding) or with `-D` system properties. If they are missing, startup fails.
- Optional, with defaults: `POSTERS_PATH_MULTIMEDIA` (`./storage/posters`), `VIDEOS_PATH_MULTIMEDIA` (`./storage/videos`), `FFPROBE_PATH_MULTIMEDIA` (`ffprobe`), `MAX_UPLOAD_SIZE_MULTIMEDIA` (`10GB`), `app.admin.password` (`admin`, password of the first admin).
- Video upload needs FFmpeg's `ffprobe` installed: the server computes `duration_seconds` with it.

The `contextLoads` test boots the full context, so it also needs a reachable MySQL and these variables.

## Architecture

Base package: `MultimediaServer` (capitalized, not `com.example...`). Lombok is on the annotation-processor path.

```
controllers/      REST controllers (/users, /genres, /movies-series, /multimedia)
services/         business logic, one per entity
repositories/     JpaRepository interfaces with derived queries
dto/<entity>/     Create<E>DTO, Update<E>DTO, Full<E>DTO records
validations/<entity>/{create,update,delete}/   one @Component per business rule
models/ + enums/  JPA entities (<E>Entity) and E-prefixed enums
config/DataInitializer   creates the first ADMIN when USERS is empty
infra/errors/     MyIntegrityValidationException + MyErrorHandler (@RestControllerAdvice) + ErrorResponse
infra/security/   SecurityConfig, CorsConfig, JWT filters, UserDetailsServiceImpl, JwtUtils
infra/services/   AuditLogService, FileStorageService, MediaProbeService (ffprobe)
```

### Conventions (follow them in new code)

- **Controllers**: field `@Autowired`, return `ResponseEntity<?>`. An empty list is `200` with `{"message":"No existen ... registrados"}`, never 404. `POST` → `201` with the `Full<E>DTO` and a `Location` header. `PUT` takes the `id` in the body (`Update<E>DTO`) → `200` with no body. `DELETE /{id}` → `200` with `{"message":"Registro eliminado correctamente"}`.
- **DTOs**: Bean Validation with Spanish messages. Update DTOs have `@NotNull Long id` and nullable fields (null = unchanged; `@Pattern(".*\\S.*")` rejects blank strings). `Full<E>DTO` has a constructor from the entity, flattens relations to ids and never exposes passwords.
- **Validations**: the service runs `List<ICreate<E>Validation>` / `IUpdate<E>Validation` / `IDelete<E>Validation` with `.forEach(v -> v.validate(...))`. Permission checks (403) and prerequisite checks use `@Order(1)` so they run before uniqueness checks.
- **Errors**: always throw `MyIntegrityValidationException(message, code, HttpStatus)`, with the HTTP status as `code`. Use 404 for a missing or soft-deleted record, 400 for duplicates (never 409) and invalid input, and 403 for permission rules. Field validation errors come back as a list of `{field, message}`.
- **Soft delete + audit**: tables with `deleted_at` are never physically deleted. `USERS` additionally sets `status = DELETED`. Queries filter `...DeletedAtIsNull` / `...StatusNot(DELETED)`. Every insert, update and delete calls `AuditLogService.create(...)` with `tableName` as the singular PascalCase entity name (`"MovieSeries"`) and the `Full<E>DTO` serialized as JSON. Cascaded deletes audit each child. `GENRES` has no audit columns, so it is deleted physically, after checking that it is not in use.
- **JSON for audit**: use Jackson 3 (`tools.jackson.databind.ObjectMapper`). The Jackson 2 on the classpath only comes with `jjwt-jackson` and cannot serialize `java.time`.
- **Files**: `FileStorageService.store(file, baseDir, allowedTypes, maxBytes)` saves `<UUID>.<ext>` under a configurable `app.storage.*` folder and returns the file name, which is all the DB stores. It is synchronized with the transaction: a rollback deletes the new file, and `deleteAfterCommit` removes a replaced file only once the change commits. Downloads return the `Resource` (Spring handles `Range` → 206). Posters are capped at 5MB in code; the global multipart limit is meant for videos.

### Security

- `/login` (JSON `{username, password}`) returns the token in the `Authentication` field. Every other request needs `Authorization: Bearer <token>`. Only `GET /multimedia/{id}/video` also accepts `?token=`, because `<video src>` cannot send headers (`JwtAuthorizationFilter`). Only `ACTIVE` users can authenticate. The login response also carries the user's `id` and `rol`, and `GET /users/me` returns the caller's `FullUserDTO`.
- CORS: `CorsConfig` exposes a `CorsConfigurationSource` that `SecurityConfig` plugs in with `.cors(...)`, so Spring Security's `CorsFilter` answers preflights before authentication and 401/403 responses carry the CORS headers too. Do not register a standalone `CorsFilter` bean: it runs after the security chain and preflights get 401.
- Rules that depend only on the role go in `SecurityConfig` (`requestMatchers(HttpMethod.X, ...).hasRole("ADMIN")`): `POST`/`PUT`/`DELETE` of genres, movies-series and multimedia, plus `GET`/`POST /users`. Rules that depend on the target record go in validations. 401 and 403 from the security layer are JSON, like the rest.
- User rules: there must always be at least one active ADMIN, enforced with a pessimistic lock on the admin rows. An ADMIN creates ADMIN/USER accounts. An ADMIN updates or deletes their own account and USER accounts, never another ADMIN's, and can promote a USER or demote themselves. A USER can read and update only their own account, cannot change their role and cannot delete any account. Renaming your own username invalidates your token, because the token subject is the username.

### Database (Flyway owns the schema)

The schema is defined only by Flyway migrations in `src/main/resources/db/migration/` (`V<n>__Description.sql`). To change the schema, add a new versioned migration and never edit an applied one. `spring.jpa.hibernate.ddl-auto=none`, and `PhysicalNamingStrategyStandardImpl` keeps the uppercase table names.

- `V1__Init_Tables.sql` creates the schema. `V2` fixes the role of the initial `admin`. `V3` makes episode uniqueness ignore soft-deleted rows.
- **Audit columns**: `created_at/created_by`, `updated_at`, `deleted_at/deleted_by`. The DB sets `created_at`/`updated_at` (mapped with `insertable = false, updatable = false`). The `*_by` columns are FKs to `USERS.user_id`, except in `USERS` itself (plain `Long`, since the first user has no creator).
- **Uniqueness that ignores deleted rows** uses stored generated columns with a UNIQUE key: `USERS.active_username`, `PERSONS.active_email` and `MULTIMEDIA.active_flag` (V3). Generated columns are not mapped in the entities.
- `PERSONS.person_id` is both the PK and the FK to `USERS.user_id` (1:1 shared PK, `@MapsId`). It is managed inside `/users` and can be absent (the initial admin has no person).
- `MOVIES_SERIES` (`type` MOVIE/SERIE) is the catalog entry; genres are many-to-many through `MOVIES_SERIES_GENRES` (`genreIds` in its DTOs). `MULTIMEDIA` rows are the playable files. A MOVIE has exactly one active file and no season/episode. A SERIE episode needs `season` + `episode_number`, unique per series. Changing a MOVIE/SERIE type is blocked while it has active files.
- `AUDIT_LOG` stores JSON before/after snapshots per table and record. It has no CRUD endpoint.
