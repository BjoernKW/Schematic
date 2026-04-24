# Vision

## What is Schematic?

**Schematic** is a simple database management UI for Spring Boot.

It is designed to be embedded into existing Spring Boot applications as a Maven dependency. Once added, it surfaces a self-contained web interface — by default at `/schematic/tables` — that lets developers and operators inspect the connected database, run ad-hoc queries, and perform basic table operations, all without leaving the application or reaching for an external database tool.

## The Problem It Solves

Every database-backed Spring Boot application eventually needs a way to look inside the database during development, staging, or incident response. The usual options are heavyweight (full-featured clients like pgAdmin or DBeaver), environment-specific (cloud console tools tied to a particular provider), or insecure in shared environments (opening database ports to allow remote connections).

Schematic takes a different approach: bring a lightweight UI to where the application already is. Because it runs inside the Spring Boot process, it inherits the application's existing authentication, network access controls, and datasource configuration. There is nothing new to install, no additional port to expose, and no credentials to share separately.

## Core Principles

**Embedded first.** Schematic is a library, not a standalone service. It is added as a single Maven dependency and auto-configures itself through Spring Boot's standard auto-configuration mechanism. The host application stays in full control and can override any default.

**Zero friction for read access.** Browsing tables, inspecting columns and data types, previewing rows, and viewing an ER diagram should require no configuration at all beyond adding the dependency.

**Progressive disclosure for write operations.** Destructive operations — `DROP TABLE` and `TRUNCATE TABLE` — are available but protected by explicit confirmation dialogs. The UI does not hide these capabilities; it makes the consequences clear before proceeding.

**Lightweight and composable.** The UI is rendered server-side with Thymeleaf and enhanced with HTMX for partial updates. There is no heavyweight JavaScript build pipeline, no separate frontend application, and no framework that competes with the host application's own frontend choices. Bootstrap provides a clean, responsive baseline that works out of the box without visual customisation.

## Current Capabilities

- **Schema introspection** — discovers all tables and columns via INFORMATION_SCHEMA, works with any JDBC-compatible database.
- **Row preview** — shows the first 10 rows of any table without writing a query.
- **Ad-hoc SQL execution** — a query input field accepts arbitrary SQL and renders the results inline.
- **Table management** — `DROP TABLE` and `TRUNCATE TABLE` with confirmation dialogs.
- **ER diagram generation** — produces a Mermaid entity-relationship diagram from the live schema. Currently PostgreSQL-specific (uses `pg_constraint` and `pg_class` system catalogs); marked as experimental in the UI.
- **Configurable path** — the URL prefix and application root path are both configurable via `application.yml`, so the UI can be mounted at any location without conflicts.

## What Schematic Is Not

Schematic is intentionally scoped to the needs of developers and operators who already have access to the running application. It is not:

- A full database administration tool — it does not manage users, permissions, indexes, or migrations.
- A data editing interface — there is no row-level insert, update, or delete UI.
- A production-hardened query workbench — the SQL execution feature is intended for development and debugging, not for production reporting or analytics workflows.
- A replacement for proper database access controls — Schematic delegates all security decisions to the host application.

## Direction

The near-term focus is on depth within the existing scope: making schema introspection richer (e.g., sorting and filtering table listings), improving ER diagram support for databases beyond PostgreSQL, and tightening the integration patterns that allow host applications to control access to Schematic's endpoints.

The longer-term goal is to remain the most frictionless way to get a useful database UI into a Spring Boot application — one dependency, sensible defaults, and no surprises.
