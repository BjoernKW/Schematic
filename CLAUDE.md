# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Schematic is a Spring Boot library that provides a simple database management UI. It's designed to be embedded into existing Spring Boot applications as a Maven dependency, offering a web interface at `/schematic/tables` to view database structure and perform basic operations.

## Build System and Commands

This project uses Maven with Spring Boot:

- **Build the project**: `./mvnw clean compile`
- **Run tests**: `./mvnw test`
- **Package the application**: `./mvnw clean package`
- **Run the application locally**: `./mvnw spring-boot:run`
- **Start PostgreSQL database**: `docker compose up -d` (uses compose.yaml)

The project uses the Maven wrapper (`mvnw`/`mvnw.cmd`) so Maven doesn't need to be installed separately.

## Key Architecture

### Auto-Configuration Pattern
The library uses Spring Boot's auto-configuration mechanism:
- `SchematicAutoConfiguration` automatically registers beans when the library is included
- Conditional beans (`@ConditionalOnMissingBean`) allow users to override defaults
- Configuration properties are handled through `SchematicProperties` with `@ConfigurationProperties("schematic")`

### Core Components
- **TablesController**: Main controller handling all database operations at `/${schematic.path:schematic}/tables`
- **Table/Column**: Simple data models representing database structure
- **SchematicProperties**: Configuration properties for customizing path and root-path

### Key Features
- **Database introspection**: Uses INFORMATION_SCHEMA queries to discover tables and columns
- **HTMX integration**: Uses `@HxRequest` for dynamic UI updates without full page reloads
- **ER diagram generation**: PostgreSQL-specific Mermaid diagram generation using system catalogs
- **SQL query execution**: Allows custom SQL queries through the UI
- **Table operations**: Support for DROP TABLE and TRUNCATE TABLE operations

### Database Support
- Primary target: PostgreSQL (includes specific ER diagram generation)
- Uses JDBC and Spring's JdbcTemplate
- Supports H2 for testing (configured in test resources)
- Database connection configured through standard Spring Boot datasource properties

## Configuration

The library is configurable via `application.yml`:

```yaml
schematic:
  path: schematic          # URL path where UI is available (default: "schematic")  
  root-path: /             # Root path of the application (default: "/")
```

## Template Structure

Uses Thymeleaf with Bootstrap and HTMX:
- Layout-based templating with `layouts/layout.html`
- Fragment-based components in `templates/fragments/`
- Bootstrap 5 and Font Awesome for styling
- HTMX for dynamic interactions

## Testing

- Uses Spring Boot Test framework
- H2 in-memory database for tests
- Test application configuration in `src/test/resources/application.yml`

## Library Distribution

This is a library project meant to be published to Maven Central:
- Excludes main application class from JAR packaging
- Uses JReleaser for automated releases
- Supports a Java 21 build

## Key conventions

- Package root: `com.bjoernkw.schematic`
- `application.yaml` (not `.properties`) for configuration
- Thymeleaf layout dialect (`thymeleaf-layout-dialect`) is available for template inheritance
- Use a newline character (`\n`) at the end of each text file. This is a common convention that can prevent issues with certain tools and editors.
- Prefer constructor injection for Spring beans. Avoid field injection (`@Autowired` on fields) as it can lead to issues with immutability and testability. Use `@RequiredArgsConstructor` for dependency injection.
- Use Spring Boot 4.x import statements and features. Avoid deprecated APIs from Spring Boot 3.x or earlier. Refer to the Spring Boot 4.0 migration guide if needed.
- Prefer Java 25 features where appropriate, but maintain readability and compatibility with Spring Boot 4.x. Avoid using preview features that may not be fully supported.
