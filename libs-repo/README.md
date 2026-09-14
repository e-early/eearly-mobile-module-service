# Vendored dependencies

This folder is a flat-file Maven repository, committed into this repo so the project can be
built and run **without VPN/Nexus access to `nexus.result.si`**. `pom.xml` points Maven at it
(`<repositories><repository><id>vendored-libs</id>...`) instead of the internal
`r3-release`/`result-lib` Nexus repositories.

## What's in here

Compiled `.jar`/`.pom` files (with matching `.sha1` checksums) for internal Result libraries this
project depends on, copied as-is from an internal build:

| Artifact | Version | Role |
|---|---|---|
| `si.result.r3:r3-rest-filter` | `1.0.1` | `?filter=` REST query parsing → JPA `Specification` (older groupId/version of the same codebase as `si.result.lib:rest-filter` below — used directly in `ScheduleServiceGrpcFacade`/`BackofficeScheduleServiceGrpcFacade`) |
| `si.result.lib:spring-boot-logger` | `1.0.1-SNAPSHOT` | structured/MDC request-response logging |
| `si.result.lib:logger` | `1.0.1-SNAPSHOT` | lower-level MDC appenders (`spring-boot-logger`'s own dependency) |
| `si.result.lib:result-logging-lib` | `1.0.1-SNAPSHOT` | Maven parent POM (pom-only, no jar) of both `logger` and `spring-boot-logger` |
| `si.result.lib:spring-boot-bricks` | `1.2.0` | pulled in transitively via `spring-boot-logger`; not imported directly by this project's own code |
| `si.result.lib:rest-filter` | `1.4.0` | pulled in transitively via `spring-boot-bricks`; not imported directly by this project's own code |

`r3-rest-filter-1.0.1.pom` was never published anywhere (only the jar was, historically) — the
copy here was written by hand from the jar's decompiled class list, declaring the ANTLR/Spring
dependencies its classes actually need. It is not an original Result-published file.

## What this is — and isn't

- **This is a stopgap to unblock building this repo, not an open-sourcing of those libraries.**
  They're shared across other internal products too (not just eEarly), and whether/how to
  actually open-source them is a separate, still-open decision.
- **No license is attached to any of these artifacts.** They are not published anywhere, carry no
  `LICENSE`/`NOTICE` file, and have no `<licenses>` entry in their POMs — they remain
  all-rights-reserved. Vendoring the compiled jars here makes this project buildable; it does not
  change that legal status.
- **No source is included** — only compiled bytecode. If you need to read, audit, or modify what
  these libraries actually do, that's not possible from this folder alone.
- If you *do* have `nexus.result.si` access, prefer reverting `pom.xml`'s `<repositories>` block
  to point at the real internal repositories instead of this folder — and consider bumping off
  the legacy `si.result.r3:r3-rest-filter:1.0.1` coordinates onto the current
  `si.result.lib:rest-filter` while you're at it, since they're the same library.

## Updating

There's no build step for this folder — to bump a version, replace the relevant
`<groupId-path>/<artifactId>/<version>/` directory with the new jar+pom+`.sha1`, and update
`libs-repo/README.md`'s table above to match.
