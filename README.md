# eEarly Mobile Service (OSS)

Backend for the eEarly mobile app: onboarding, biometric measurement ingestion (REST + gRPC),
Keycloak-backed auth, and EHRbase (openEHR) integration.

Modules:

- `eearly` — Spring Boot app (run this)
- `eearly-mobile` — mobile-facing domain/services
- `eearly-backoffice` — backoffice-facing domain/services
- `eearly-common` — shared code, Keycloak/EHRbase clients, proto definitions

---

## Prerequisites

| Requirement | Version / note |
|-------------|----------------|
| Java | 22 |
| Maven | 3.9+ (wrapper `./mvnw` included) |
| Docker | local Postgres + Mailhog + nginx |
| Nexus | *optional* — `https://nexus.result.si`; not required to build, see below |

### Maven configuration

This project's internal (`si.result.r3`/`si.result.lib`) dependencies are vendored into
[`libs-repo/`](libs-repo/README.md), so building (`mvn package`) does **not** require Nexus
access or `~/.m2/settings.xml` credentials. The settings below are only needed if you have
`nexus.result.si` access and want to `mvn deploy` (publish) to the internal
`eearly-backend-release`/`eearly-backend-snapshot` repos:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>si.result.r3.r3-snapshot</id>
      <username>[your_username]</username>
      <password>[your_password]</password>
    </server>
    <server>
      <id>si.result.r3.r3-release</id>
      <username>[your_username]</username>
      <password>[your_password]</password>
    </server>
  </servers>
</settings>
```

Use your domain username/password.

### Related services (local E2E)

| Service | How to run | Host ports |
|---------|------------|------------|
| **Mobile Keycloak** | `eearly-mobile-module-keycloak` → see its README | `9091` (app), `9190` (mgmt) |
| **EHRbase** | `eearly-ehr-module-opensource` → `docker compose up -d` | `8000` (API), `9092` (EHR Keycloak) |
| **This service's infra** | this repo → `docker compose up -d` in `docker/` | DB `5433`, Mailhog `8025`/`1025`, nginx `80` |
| **This service (app)** | Maven on host, see below | REST `8081`, gRPC `8082` |

`eearly-admin-module-service-opensource` can be run against this service for the full apnea
analysis flow — see that repo's README.

---

## Quick start

### 1. Infrastructure

```bash
cd /path/to/eearly-mobile-module-service-opensource
docker compose -f docker/docker-compose.yml up -d
```

Starts:

- Postgres on `localhost:5433` (db/user/password `postgres`, per the top-level `.env`)
- Mailhog on `localhost:8025` (UI) / `1025` (SMTP)
- An nginx reverse proxy on `localhost:80` (fronts REST/gRPC + Keycloak — optional for local dev, safe to ignore if you call `8081`/`8082` directly)

### 2. Mobile Keycloak

Start `eearly-mobile-module-keycloak` locally (see its README) so it's reachable at
`http://localhost:9091`, realm `eearly-mobile`. Fetch client secrets from the admin console
(**Clients → <client> → Credentials**):

- `eearly-user-administrator` → `KEYCLOAK_ADMIN_CLIENT_SECRET`
- `admin-service` → used by `eearly-admin-module-service` (`MOBILE_KEYCLOAK_ADMIN_CLIENT_SECRET`), not needed by this service itself

### 3. Environment

The app reads everything from environment variables (no `application-local.yaml` profile) —
Spring does **not** auto-load `.env`, so export these before starting the JAR/`spring-boot:run`:

```bash
# Database (matches docker/docker-compose.yml)
export DB_URL=jdbc:postgresql://localhost:5433/postgres
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export DB_SCHEMA=eearly_mobile

# Mobile Keycloak (this service's own users/tokens)
export KEYCLOAK_URL=http://localhost:9091
export KEYCLOAK_REALM=eearly-mobile
export KEYCLOAK_ADMIN_CLIENT_ID=eearly-user-administrator
export KEYCLOAK_ADMIN_CLIENT_SECRET=<from Keycloak Credentials tab>
export KEYCLOAK_MOBILE_CLIENT_ID=eearly-mobile
export KEYCLOAK_ADMIN_SERVICE_CLIENT_ID=admin-service

# Patient onboarding (mobile app deep links / QR flow)
export ONBOARDING_API_BASE_URL=http://localhost:8081
export ONBOARDING_BASE_URL=http://localhost:8081
export MOBILE_APP_STORE_FALLBACK_URL=https://eearly.result.si
export MOBILE_KEYCLOAK_BASE_URL=http://localhost:9091
export MOBILE_KEYCLOAK_HOST=localhost:9091

# EHRbase (openEHR) — from eearly-ehr-module-opensource
export EHR_BASE_URL=http://localhost:8000
export EHR_KEYCLOAK_BASE_URL=http://localhost:9092
export EHR_KEYCLOAK_CLIENT_SECRET=<from EHR Keycloak realm-config>

# Dexcom sensor integration — optional, only needed if you exercise that flow
export DEXCOM_REDIRECT_URI=http://localhost:8081/dexcom/callback
export DEXCOM_CLIENT_ID=<sandbox client id>
export DEXCOM_CLIENT_SECRET=<sandbox client secret>

# Logging
export LOGGING_FILE_PATH=/tmp/eearly-mobile.log
```

| Variable | Default in code | Purpose |
|----------|-----------------|---------|
| `DB_SCHEMA` | `eearly_mobile` | Postgres schema (Flyway is disabled — schema/tables must already exist or be created via the `postgres` image init, see note below) |
| `KEYCLOAK_REALM` | `eearly-mobile` | This service's realm |
| `KEYCLOAK_MOBILE_CLIENT_ID` | `eearly-mobile` | Public client used by the mobile app |
| `KEYCLOAK_ADMIN_SERVICE_CLIENT_ID` | `admin-service` | Client `eearly-admin-module-service` authenticates as |
| `MOBILE_KEYCLOAK_REALM` | `eearly-mobile` | Realm used for onboarding links |
| `MOBILE_KEYCLOAK_CLIENT_ID` | `eearly-mobile` | Client used for onboarding links |
| `EHR_KEYCLOAK_REALM` | `eearly-ehrbase` | EHR Keycloak realm |
| `EHR_KEYCLOAK_CLIENT_ID` | `ehrbase` | EHR Keycloak client |

Firebase push notifications use a service-account file already checked in at
`eearly-common/src/main/resources/eearly-81b14-7704d412928d.json`; replace it with your own
Firebase project's service account if you need working push notifications, or ignore it for a
plain REST/gRPC/EHR flow.

`spring.flyway.enabled` is `false` by default in `application.yml` (migrations are applied out of
band in staging/production). For a local run, enable it so the bundled migration
(`eearly-common/src/main/resources/db/migration/common/V1__consolidated_schema.sql`) creates the
`DB_SCHEMA` schema and tables for you:

```bash
export SPRING_FLYWAY_ENABLED=true
```

### 4. Generate proto / gRPC classes

```bash
./mvnw generate-sources
```

(Also runs automatically as part of `package`/`install`.)

### 5. Build

```bash
./mvnw clean install -DskipTests
```

### 6. Run

```bash
./mvnw -pl eearly -am spring-boot:run
```

or, after building a jar:

```bash
java -jar eearly/target/eearly.jar
```

- REST: `http://localhost:8081`
- gRPC: `localhost:8082`

---

## Getting a JWT for gRPC calls (Postman)

1. Ask a Keycloak admin to create an account for you, or use your own Keycloak account.
2. In Postman, create an environment for Keycloak access with variables:

   ```yaml
   server: https://keycloak.result.si   # or http://localhost:9091 for local Keycloak
   realm: eEarly                        # or eearly-mobile for local Keycloak
   clientId: eearly-mobile
   user: your keycloak user
   password: your keycloak password
   ```

   ![img.png](img.png)

3. Create a POST request:
   - Select the environment created above.
   - URL: `{{server}}/realms/{{realm}}/protocol/openid-connect/token`
   - Body parameters as shown:

     ![img_1.png](img_1.png)

   - Send. Response:

     ![img_2.png](img_2.png)

   - Check your email for the OTP Keycloak sends, copy it into the `email_otp` body parameter, and resend the request. Response:

     ![img_3.png](img_3.png)

   - Copy the `access_token` value.
4. Use that value as a Bearer token (Authorization tab) on your gRPC request — it should now be authenticated.

---

## Generating EHRbase classes

EHRbase (openEHR) classes are generated by running the generator plugin inside the `eearly-common` module:

```bash
./mvnw -pl eearly-common generate-sources
```

---

## Troubleshooting

### Nexus / Maven resolve failure

This project's internal (`si.result.r3`/`si.result.lib`) dependencies are vendored into
[`libs-repo/`](libs-repo/README.md), so `mvn package` does **not** require VPN/Nexus access —
if you're hitting a resolve failure, it's most likely a public artifact (Maven Central) instead;
retry, or check your network. VPN + `~/.m2/settings.xml` credentials are only needed for
`mvn deploy` (publishing to the internal repos).

### App fails on startup with a missing/unset environment variable

Most config keys in `eearly/src/main/resources/application.yml` have no default — double-check
the exports in [Environment](#3-environment) above, in particular `DB_URL`, `DB_USERNAME`,
`DB_PASSWORD`, `KEYCLOAK_URL`, `EHR_BASE_URL`, `EHR_KEYCLOAK_BASE_URL`,
`EHR_KEYCLOAK_CLIENT_SECRET`, `ONBOARDING_API_BASE_URL`, `ONBOARDING_BASE_URL`,
`MOBILE_APP_STORE_FALLBACK_URL`, `MOBILE_KEYCLOAK_BASE_URL`, `MOBILE_KEYCLOAK_HOST`.

### `relation does not exist` / schema errors

Flyway is disabled by default — set `SPRING_FLYWAY_ENABLED=true` (see step 3) so the bundled
migration creates the `DB_SCHEMA` schema and tables before the app tries to use them.

### 401 from Keycloak

Confirm `KEYCLOAK_URL`/`MOBILE_KEYCLOAK_BASE_URL` point at a running Keycloak with the
`eearly-mobile` realm imported, and that client secrets match what's configured in that realm's
**Credentials** tab.

---

## Related repositories

- [eearly-mobile-module-keycloak](https://github.com/e-early/eearly-mobile-module-keycloak.git) — this service's Keycloak realm/image
- [eearly-admin-module-service-opensource](https://github.com/e-early/eearly-admin-module-service.git) — admin backend that talks to this service (measurements, onboarding)
- [eearly-ehr-module-opensource](https://github.com/e-early/eearly-ehr-module-service.git) — EHRbase + EHR Keycloak
