# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Commands

```bash
./mvnw spring-boot:run          # Run application (starts Docker Compose postgres automatically)
./mvnw test                     # Run all tests (requires Docker for Testcontainers)
./mvnw test -Dtest=ClassName    # Run a single test class
./mvnw clean package            # Build fat JAR
./mvnw clean package -DskipTests  # Build without running tests
```

## Architecture

This is a **Spring Boot 4.0.5** library (Java 21)**Schematic** providing a database management UI for Spring Boot.

**Embedded first.** Schematic is a library, not a standalone service. It is added as a single Maven dependency and auto-configures itself through Spring Boot's standard auto-configuration mechanism. The host application stays in full control and can override any default.

**Zero friction for read access.** Browsing tables, inspecting columns and data types, previewing rows, and viewing an ER diagram should require no configuration at all beyond adding the dependency.

**Progressive disclosure for write operations.** Destructive operations — `DROP TABLE` and `TRUNCATE TABLE` — are available but protected by explicit confirmation dialogs. The UI does not hide these capabilities; it makes the consequences clear before proceeding.

**Lightweight and composable.** The UI is rendered server-side with Thymeleaf and enhanced with HTMX for partial updates. There is no heavyweight JavaScript build pipeline, no separate frontend application, and no framework that competes with the host application's own frontend choices. Bootstrap provides a clean, responsive baseline that works out of the box without visual customisation.

## Key conventions

- Package root: `com.bjoernkw.schematic`
- `application.yaml` (not `.properties`) for configuration
- Thymeleaf layout dialect (`thymeleaf-layout-dialect`) is available for template inheritance
- Use a newline character (`\n`) at the end of each text file. This is a common convention that can prevent issues with certain tools and editors.
- Prefer constructor injection for Spring beans. Avoid field injection (`@Autowired` on fields) as it can lead to issues with immutability and testability. Use `@RequiredArgsConstructor` for dependency injection.
- Use Spring Boot 4.x import statements and features. Avoid deprecated APIs from Spring Boot 3.x or earlier. Refer to the Spring Boot 4.0 migration guide if needed.
- Prefer Java 25 features where appropriate, but maintain readability and compatibility with Spring Boot 4.x. Avoid using preview features that may not be fully supported.
