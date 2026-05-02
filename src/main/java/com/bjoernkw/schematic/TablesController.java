package com.bjoernkw.schematic;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/${schematic.path:schematic}/tables")
public class TablesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TablesController.class);

    private static final String TABLE_VIEW_MODEL_NAME = "tables";

    private static final String ER_DIAGRAM_VIEW_MODEL_NAME = "erDiagram";

    private static final String TABLE_VIEW_FRAGMENT_NAME = "schematic-fragments/tables";

    private static final String ER_DIAGRAM_RESULT_SET_COLUMN_NAME = "mermaid_diagram_line";

    private static final String INDEX_VIEW_NAME = "schematic-index";

    private final JdbcClient jdbcClient;

    private final DataSource dataSource;

    private final SchematicTableFilter tableFilter;

    private final SchematicProperties schematicProperties;

    public TablesController(JdbcClient jdbcClient, DataSource dataSource, SchematicTableFilter tableFilter, SchematicProperties schematicProperties) {
        this.jdbcClient = jdbcClient;
        this.dataSource = dataSource;
        this.tableFilter = tableFilter;
        this.schematicProperties = schematicProperties;
    }

    @GetMapping
    public String showDatabaseStructure(Model model) {
        List<Table> tables = getTables();
        Set<String> visibleTableNames = tables.stream()
                .map(Table::getTableName)
                .collect(Collectors.toSet());

        model.addAttribute(TABLE_VIEW_MODEL_NAME, tables);
        model.addAttribute(ER_DIAGRAM_VIEW_MODEL_NAME, generateERDiagram(visibleTableNames));

        return INDEX_VIEW_NAME;
    }

    @GetMapping(params = "sqlQuery")
    @HxRequest
    public String queryDatabase(@RequestParam String sqlQuery, Model model) {
        List<Table> tables = new ArrayList<>();
        Table queryResultTable = new Table();
        tables.add(queryResultTable);

        queryResultTable.setTableName("queryResult");
        queryResultTable.setQueryResult(true);

        List<Map<String, Object>> queryResultRows = jdbcClient.sql(sqlQuery).query().listOfRows();
        queryResultTable.setRows(queryResultRows);

        List<Column> columns = new ArrayList<>();
        queryResultRows.stream().findFirst().ifPresent(row -> row.forEach((columnKey, columnValue) -> {
            Column column = new Column();
            column.setColumnName(columnKey);

            columns.add(column);
        }));
        queryResultTable.setColumns(columns);

        tables.addAll(getTables());

        model.addAttribute(TABLE_VIEW_MODEL_NAME, tables);

        return TABLE_VIEW_FRAGMENT_NAME;
    }

    @DeleteMapping("/{tableName}")
    @HxRequest
    public String dropTable(@PathVariable String tableName, Model model, HttpServletResponse response) {
        if (!isTableVisibleSafely(tableName)) {
            model.addAttribute(TABLE_VIEW_MODEL_NAME, getTables());
            return TABLE_VIEW_FRAGMENT_NAME;
        }

        if (!isOperationPermittedSafely(tableName, TableOperation.DROP)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute("error", "Operation not permitted on table: " + tableName);
            model.addAttribute(TABLE_VIEW_MODEL_NAME, getTables());
            return TABLE_VIEW_FRAGMENT_NAME;
        }

        List<Table> availableTables = getTables();
        if (availableTables.stream().anyMatch(table -> table.getTableName().equals(tableName))) {
            jdbcClient.sql("DROP TABLE " + tableName).update();
        }

        model.addAttribute(TABLE_VIEW_MODEL_NAME, getTables());

        return TABLE_VIEW_FRAGMENT_NAME;
    }

    @DeleteMapping("/{tableName}/truncate")
    @HxRequest
    public String truncateTable(@PathVariable String tableName, Model model, HttpServletResponse response) {
        if (!isTableVisibleSafely(tableName)) {
            model.addAttribute(TABLE_VIEW_MODEL_NAME, getTables());
            return TABLE_VIEW_FRAGMENT_NAME;
        }

        if (!isOperationPermittedSafely(tableName, TableOperation.TRUNCATE)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute("error", "Operation not permitted on table: " + tableName);
            model.addAttribute(TABLE_VIEW_MODEL_NAME, getTables());
            return TABLE_VIEW_FRAGMENT_NAME;
        }

        List<Table> availableTables = getTables();
        if (availableTables.stream().anyMatch(table -> table.getTableName().equals(tableName))) {
            jdbcClient.sql("TRUNCATE TABLE " + tableName).update();
        }

        model.addAttribute(TABLE_VIEW_MODEL_NAME, getTables());

        return TABLE_VIEW_FRAGMENT_NAME;
    }

    @ExceptionHandler(SQLException.class)
    @HxRequest
    public String error(SQLException sqlException, Model model) {
        model.addAttribute("error", sqlException.getMessage());
        model.addAttribute(TABLE_VIEW_MODEL_NAME, getTables());

        return TABLE_VIEW_FRAGMENT_NAME;
    }

    private List<Table> getTables() {
        List<Table> tables = jdbcClient
                .sql("SELECT table_name FROM INFORMATION_SCHEMA.Tables WHERE lower(table_schema) = 'public' AND table_type = 'BASE TABLE'")
                .query(new BeanPropertyRowMapper<>(Table.class))
                .list();

        tables = tables.stream()
                .filter(table -> isTableVisibleSafely(table.getTableName()))
                .collect(Collectors.toList());

        tables.forEach(table -> {
            table.setColumns(
                    jdbcClient
                            .sql("SELECT column_name, data_type FROM INFORMATION_SCHEMA.Columns WHERE table_name = ?")
                            .param(table.getTableName())
                            .query(new BeanPropertyRowMapper<>(Column.class))
                            .list()
            );
            table.setRows(
                    jdbcClient
                            .sql("SELECT * FROM " + table.getTableName() + " FETCH FIRST " + schematicProperties.getPreviewRowLimit() + " ROWS ONLY")
                            .query()
                            .listOfRows()
            );
            table.setDropPermitted(isOperationPermittedSafely(table.getTableName(), TableOperation.DROP));
            table.setTruncatePermitted(isOperationPermittedSafely(table.getTableName(), TableOperation.TRUNCATE));
        });

        return tables;
    }

    private boolean isTableVisibleSafely(String tableName) {
        try {
            return tableFilter.isTableVisible(tableName);
        } catch (Exception e) {
            LOGGER.warn("SchematicTableFilter threw an exception evaluating visibility for table '{}': {}", tableName, e.getMessage());
            return false;
        }
    }

    private boolean isOperationPermittedSafely(String tableName, TableOperation operation) {
        try {
            return tableFilter.isOperationPermitted(tableName, operation);
        } catch (Exception e) {
            LOGGER.warn("SchematicTableFilter threw an exception evaluating operation '{}' for table '{}': {}", operation, tableName, e.getMessage());
            return false;
        }
    }

    private String generateERDiagram(Set<String> visibleTableNames) {
        String driverClassName = "";
        try {
            driverClassName = DriverManager.getDriver(dataSource.getConnection().getMetaData().getURL()).getClass().toString();
        } catch (Exception e) {
            LOGGER.warn("Could not determine database driver: {}", e.getMessage());
        }

        if (driverClassName.equals("class org.postgresql.Driver")) {
            // See https://www.cybertec-postgresql.com/en/er-diagrams-with-sql-and-mermaid/#
            String sqlQuery = """
                SELECT E'erDiagram\\n' AS mermaid_diagram_line
                UNION ALL
                SELECT
                    format(E'\\t%s {\\n%s\\n\\t}\\n',
                        c.relname,
                        string_agg(format(E'\\t\\t%s %s',
                            t.typname,
                            a.attname
                        ), E'\\n'))
                FROM
                    pg_class c
                    JOIN pg_namespace n ON n.oid = c.relnamespace
                    LEFT JOIN pg_attribute a ON c.oid = a.attrelid AND a.attnum > 0 AND NOT a.attisdropped
                    LEFT JOIN pg_type t ON a.atttypid = t.oid
                WHERE
                    c.relkind IN ('r', 'p')
                    AND NOT c.relispartition
                    AND n.nspname !~ '^pg_' AND n.nspname <> 'information_schema'
                GROUP BY c.relname
                UNION ALL
                SELECT
                    format(E'\\t%s }|..|| %s : %s\\n', c1.relname, c2.relname, c.conname)
                FROM
                    pg_constraint c
                    JOIN pg_class c1 ON c.conrelid = c1.oid AND c.contype = 'f'
                    JOIN pg_class c2 ON c.confrelid = c2.oid
                WHERE
                    NOT c1.relispartition AND NOT c2.relispartition;
            """;

            StringBuilder output = new StringBuilder();
            List<Map<String, Object>> queryResultRows = jdbcClient.sql(sqlQuery).query().listOfRows();
            for (Map<String, Object> queryResultRow : queryResultRows) {
                String line = String.valueOf(queryResultRow.get(ER_DIAGRAM_RESULT_SET_COLUMN_NAME));
                if (shouldIncludeERDiagramLine(line, visibleTableNames)) {
                    output.append(line);
                }
            }

            return output.toString();
        }

        return generateERDiagramFromInformationSchema(visibleTableNames);
    }

    private boolean shouldIncludeERDiagramLine(String line, Set<String> visibleTableNames) {
        if (line == null) {
            return false;
        }
        if (line.startsWith("erDiagram")) {
            return true;
        }
        String trimmed = line.stripLeading();
        if (trimmed.contains("}|..||")) {
            // FK line format: "source_table }|..|| target_table : constraint_name\n"
            String[] parts = trimmed.split("\\s+");
            return parts.length >= 3
                    && visibleTableNames.contains(parts[0])
                    && visibleTableNames.contains(parts[2]);
        }
        // Entity block format: "tableName {\n..."
        String tableName = trimmed.split("\\s+")[0];
        return visibleTableNames.contains(tableName);
    }

    private String generateERDiagramFromInformationSchema(Set<String> visibleTableNames) {
        String tableColumnQuery = """
                SELECT
                    t.table_name,
                    c.column_name,
                    c.data_type
                FROM
                    INFORMATION_SCHEMA.tables t
                    JOIN INFORMATION_SCHEMA.columns c
                        ON t.table_name = c.table_name
                        AND t.table_schema = c.table_schema
                WHERE
                    lower(t.table_schema) = 'public'
                    AND t.table_type = 'BASE TABLE'
                ORDER BY
                    t.table_name, c.ordinal_position
                """;

        List<Map<String, Object>> tableColumnRows = jdbcClient.sql(tableColumnQuery).query().listOfRows();

        Map<String, List<Map<String, Object>>> tableColumns = new LinkedHashMap<>();
        for (Map<String, Object> row : tableColumnRows) {
            String tableName = (String) row.get("table_name");
            if (visibleTableNames.contains(tableName)) {
                tableColumns.computeIfAbsent(tableName, k -> new ArrayList<>()).add(row);
            }
        }

        List<Map<String, Object>> foreignKeys = new ArrayList<>();
        try {
            String fkQuery = """
                    SELECT
                        kcu.table_name AS source_table,
                        kcu2.table_name AS target_table,
                        kcu.constraint_name
                    FROM
                        INFORMATION_SCHEMA.referential_constraints rc
                        JOIN INFORMATION_SCHEMA.key_column_usage kcu
                            ON rc.constraint_name = kcu.constraint_name
                        JOIN INFORMATION_SCHEMA.key_column_usage kcu2
                            ON rc.unique_constraint_name = kcu2.constraint_name
                    WHERE
                        lower(kcu.table_schema) = 'public'
                    GROUP BY
                        kcu.table_name, kcu2.table_name, kcu.constraint_name
                    """;
            foreignKeys = jdbcClient.sql(fkQuery).query().listOfRows();
        } catch (Exception e) {
            LOGGER.warn("Could not retrieve foreign key relationships for ER diagram: {}", e.getMessage());
        }

        StringBuilder diagram = new StringBuilder("erDiagram\n");

        for (Map.Entry<String, List<Map<String, Object>>> entry : tableColumns.entrySet()) {
            diagram.append("\t").append(entry.getKey()).append(" {\n");
            for (Map<String, Object> col : entry.getValue()) {
                String dataType = String.valueOf(col.getOrDefault("data_type", "unknown")).replace(" ", "_");
                diagram.append("\t\t").append(dataType).append(" ").append(col.get("column_name")).append("\n");
            }
            diagram.append("\t}\n");
        }

        for (Map<String, Object> fk : foreignKeys) {
            String source = (String) fk.get("source_table");
            String target = (String) fk.get("target_table");
            if (visibleTableNames.contains(source) && visibleTableNames.contains(target)) {
                diagram
                        .append("\t")
                        .append(source)
                        .append(" }|..|| ")
                        .append(target)
                        .append(" : ")
                        .append(fk.get("constraint_name"))
                        .append("\n");
            }
        }

        return diagram.toString();
    }
}