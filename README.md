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
      <id>e-early-backend-snapshot</id>
      <username>[your_username]</username>
      <password>[your_password]</password>
    </server>
    <server>
      <id>e-early-backend-release</id>
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
Spring does **not** auto-load `.env`. Copy [`.env.example`](.env.example) to `.env`, fill in the
placeholders (Keycloak/EHR client secrets — see the comments in that file for where to get each
one), then:

```bash
set -a && source .env && set +a
```

before starting the JAR/`spring-boot:run`. Two things in there are easy to get wrong and worth
calling out specifically:

- `EHR_BASE_URL` must be the full REST API path, not just the host, **and** end with a trailing
  slash — `EhrbaseClient` uses it verbatim as its `WebClient` base URL and appends relative paths
  like `ehr/{id}`; without the trailing slash, URI resolution drops the last path segment (`v1`)
  instead of appending to it.
- `EHR_KEYCLOAK_CLIENT_SECRET` / `KEYCLOAK_ADMIN_CLIENT_SECRET`: the values checked into each
  Keycloak repo's `realm-config` are not reliably what's actually active on a running instance —
  fetch the real one from that Keycloak's own admin console (Clients → *client* → Credentials)
  once it's up.

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

Push notifications (Firebase) are **not included** in this OSS build — `sendNotificationForToken`,
`notifyAboutNewSchedules`, and `sendMeasurementReminder` are no-ops (they log and return, no
Firebase SDK/credentials involved). The gRPC/REST methods and their proto contracts are unchanged,
so existing clients still work; they just won't receive a push notification.

`spring.flyway.enabled` is `false` by default in `application.yml` (migrations are applied out of
band in staging/production). `.env.example` sets `SPRING_FLYWAY_ENABLED=true` for local runs so
the bundled migration (`eearly-common/src/main/resources/db/migration/common/V1__consolidated_schema.sql`)
creates the `DB_SCHEMA` schema and tables for you.

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

## Example: push and read back a measurement (curl + grpcurl)

This creates a user, gets a patient JWT, and writes a heart-rate + SpO₂ measurement to EHRbase —
entirely against this service, with no admin-service involved except to obtain the one token
needed below.

### 1. Get a token authorized to create users

`POST /api/v1/onboarding/create-user` requires the realm role `onboard-patient` — it is **not**
public despite sitting under `/api/v1/onboarding/**`, which is otherwise permitAll (see
`WebSecurityConfiguration`: the more specific `create-user` matcher is declared first and wins).
The `admin-service` client's service account carries that role, so get a token as that client:

```bash
export ADMIN_SERVICE_SECRET=<from mobile Keycloak: Clients -> admin-service -> Credentials>

export SERVICE_TOKEN=$(curl -s -X POST \
  http://localhost:9091/realms/eearly-mobile/protocol/openid-connect/token \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=client_credentials' \
  -d 'client_id=admin-service' \
  -d "client_secret=${ADMIN_SERVICE_SECRET}" \
  | jq -r '.access_token')
```

### 2. Create a user

```bash
curl -s -X POST http://localhost:8081/api/v1/onboarding/create-user \
  -H "Authorization: Bearer ${SERVICE_TOKEN}" \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Test","lastName":"User","email":"test.'"$(date +%s)"'@example.com"}' \
  | tee /tmp/user.json | jq .

export ONBOARDING_ID=$(jq -r '.user.id' /tmp/user.json)
```

`onboardingUrl` in the response is just `http://localhost:8081/{userId}` — not a path you can call
directly, and not the `/onboarding/{id}/configuration` shape a quick skim might suggest. Use
`user.id` instead.

### 3. Get the patient JWT

```bash
curl -s "http://localhost:8081/api/v1/onboarding/${ONBOARDING_ID}/configuration" \
  | tee /tmp/config.json | jq .

export ACCESS_TOKEN=$(jq -r '.accessToken' /tmp/config.json)
```

### 4. Push measurements (gRPC)

One measurement type per call — mixing types in one `CreateMeasurement` call only writes the
first type's composition. The two type ids below are seeded by this repo's own Flyway migration
(`V1__consolidated_schema.sql`), so they exist as long as `SPRING_FLYWAY_ENABLED=true` was set on
first run.

```bash
# Heart rate
grpcurl -plaintext \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -d '{"measurements":[{"measurementTypeId":"3d51f149-4eb2-42af-b217-8f5a754968eb","value":72,"measuredAt":"2026-09-15 10:00:00.000","measurementBatchId":"hr-1"}]}' \
  localhost:8082 si.result.eearly.genproto.MeasurementService/CreateMeasurement

# SpO2 (separate call, separate batch id)
grpcurl -plaintext \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -d '{"measurements":[{"measurementTypeId":"0ecc92b4-dd67-4ed0-952d-cbbd05026332","value":97,"measuredAt":"2026-09-15 10:00:00.000","measurementBatchId":"spo2-1"}]}' \
  localhost:8082 si.result.eearly.genproto.MeasurementService/CreateMeasurement
```

Both calls should return `{}` on success — that's the real confirmation the write reached EHRbase.

### 5. Read the measurements back

Your own patient token works here too — the gRPC security config allows `admin-service` *or*
any authenticated caller for `GetMeasurementsForUser`, not just admin. `GetMeasurements` (no
`userId`) resolves the user from the JWT itself instead, same request shape otherwise.

**Careful: this is a different timestamp format than `CreateMeasurement` used above** —
`startDateTime`/`endDateTime` here are parsed as `OffsetDateTime`/`ZonedDateTime` (ISO-8601 with
a zone, e.g. `...Z`), not the space-separated `yyyy-MM-dd HH:mm:ss.SSS` `measuredAt` uses.

```bash
grpcurl -plaintext \
  -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  -d "{\"userId\":\"${ONBOARDING_ID}\",\"measurementTypes\":[\"HEART_RATE\",\"OXYGEN_SATURATION\"],\"startDateTime\":\"2026-09-15T09:00:00Z\",\"endDateTime\":\"2026-09-15T11:00:00Z\",\"page\":0,\"size\":10}" \
  localhost:8082 si.result.eearly.genproto.MeasurementService/GetMeasurementsForUser
```

The response's `measurements` array should contain the entries you pushed in step 4, matching
`measurementTypeId`, `value`, `measuredAt`, and `measurementBatchId`.

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
(`LOGGING_FILE_PATH` is the exception — it now defaults to `./logs/eearly-mobile.log` if unset.)

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
