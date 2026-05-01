# Requirements

## Functional Requirements

| ID     | Title                              | User Story                                                                                                                                                               | Priority | Status      |
|--------|------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|-------------|
| FR-001 | Add as Maven dependency            | As a developer, I want to add Schematic as a single Maven dependency so that the database UI is auto-configured without any additional setup.                            | High     | Implemented |
| FR-002 | Browse database schema             | As a developer, I want to view all tables and their columns in the connected database so that I can understand the schema without a separate database client.            | High     | Implemented |
| FR-003 | Preview table data                 | As a developer, I want to view the first rows of a table so that I can quickly verify data is present and correctly structured.                                          | High     | Implemented |
| FR-004 | Execute ad-hoc SQL queries         | As a developer, I want to execute custom SQL queries through the UI so that I can investigate data and troubleshoot issues without a separate database client.           | High     | Implemented |
| FR-005 | Drop a table                       | As an operator, I want to drop a table after confirming my intent so that I can remove unwanted tables without accidentally destroying data.                             | Medium   | Implemented |
| FR-006 | Truncate a table                   | As an operator, I want to truncate a table after confirming my intent so that I can clear all rows without accidentally deleting data from the wrong table.              | Medium   | Implemented |
| FR-007 | View ER diagram                    | As a developer, I want to view an auto-generated entity-relationship diagram so that I can understand relationships between tables at a glance.                          | Medium   | Implemented |
| FR-008 | Copy ER diagram source             | As a developer, I want to copy the Mermaid source of the ER diagram to the clipboard so that I can embed it in architecture documentation.                               | Low      | Implemented |
| FR-009 | Configure UI mount path            | As a developer, I want to configure the URL path where the Schematic UI is mounted so that it does not conflict with my application's existing routes.                   | Medium   | Implemented |
| FR-010 | Sort and filter table listings     | As a developer, I want to sort and filter the list of tables by name so that I can locate relevant tables quickly in a large schema.                                     | Medium   | Implemented |
| FR-011 | ER diagram for non-PostgreSQL databases | As a developer, I want ER diagram generation to work with databases beyond PostgreSQL so that I can use the full feature set regardless of my database vendor.      | Low      | Implemented |

## Non-Functional Requirements

| ID      | Title                        | Requirement                                                                                                                               | Category    | Priority | Status |
|---------|------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|-------------|----------|--------|
| NFR-001 | Page load performance        | All UI pages must complete initial render within 2 seconds on a developer workstation connected to a local database.                      | Performance | High     | Open   |
| NFR-002 | Zero required configuration  | The UI must be fully operational for all read operations after adding the Maven dependency, with no mandatory entries in application.yml. | Usability   | High     | Open   |
| NFR-003 | In-process operation         | Schematic must run entirely within the host Spring Boot process and must not open any additional network ports.                           | Security    | High     | Open   |
| NFR-004 | Dependency footprint         | The library must not introduce transitive dependency conflicts with a standard Spring Boot 3.x or 4.x application.                        | Reliability | High     | Open   |
| NFR-005 | Destructive-operation safety | Every DROP TABLE and TRUNCATE TABLE operation must display an explicit confirmation dialog before executing.                              | Security    | High     | Open   |
| NFR-006 | Responsive layout            | The web interface must be usable without horizontal scrolling on screen widths from 768 px (tablet) to 1920 px (desktop).                 | Usability   | Medium   | Open   |

## Constraints

| ID    | Title                       | Constraint                                                                                                                                                                       | Category  | Priority | Status |
|-------|-----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------|----------|--------|
| C-001 | Distribution channel        | The library must be distributed as a JAR artifact on Maven Central.                                                                                                              | Technical | High     | Open   |
| C-002 | Java version                | The library must target Java 21 LTS as the primary runtime; a separate Maven build profile must maintain compatibility with Java 11.                                             | Technical | High     | Open   |
| C-003 | Host framework integration  | The library must integrate exclusively through Spring Boot's auto-configuration mechanism and must not require changes to the host application's main class or bean definitions. | Technical | High     | Open   |
| C-004 | Database access layer       | All database operations must use Spring's JdbcClient or JdbcTemplate; no JPA or ORM dependency may be introduced.                                                                | Technical | High     | Open   |
| C-005 | Datasource reuse            | The library must use the host application's existing configured datasource and must not require a dedicated secondary datasource.                                                | Technical | High     | Open   |
| C-006 | ER diagram scope            | ER diagram generation is limited to PostgreSQL databases in the current release; other JDBC databases show no diagram.                                                           | Technical | Medium   | Open   |
| C-007 | INFORMATION_SCHEMA reliance | Schema introspection and basic table operations must rely solely on standard INFORMATION_SCHEMA views so they work with any JDBC-compatible database.                            | Technical | High     | Open   |
| C-008 | No frontend build step      | The UI must require no Node.js or npm build pipeline; all frontend assets must be served as WebJars or loaded from a CDN within the Spring Boot application.                     | Technical | High     | Open   |
