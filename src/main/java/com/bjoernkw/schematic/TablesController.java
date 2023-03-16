package com.bjoernkw.schematic;

import io.github.wimdeblauwe.hsbt.mvc.HxRequest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/schematic/tables")
public class TablesController {

    private static final String TABLE_VIEW_MODEL_NAME = "tables";

    private static final String ER_DIAGRAM_VIEW_MODEL_NAME = "erDiagram";

    private static final String TABLE_VIEW_FRAGMENT_NAME = "fragments/tables";

    private static final String ER_DIAGRAM_RESULT_SET_COLUMN_NAME = "mermaid_diagram_line";

    private final JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    public TablesController(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @GetMapping
    public String showDatabaseStructure(Model model) {
        model.addAttribute(
                TABLE_VIEW_MODEL_NAME,
                getTables()
        );
        model.addAttribute(
                ER_DIAGRAM_VIEW_MODEL_NAME,
                generateERDiagram()
        );

        return "index";
    }

    @GetMapping(params = "sqlQuery")
    @HxRequest
    public String queryDatabase(@RequestParam String sqlQuery, Model model) {
        List<Table> tables = new ArrayList<>();
        Table queryResultTable = new Table();
        tables.add(queryResultTable);

        queryResultTable.setTableName("queryResult");
        queryResultTable.setQueryResult(true);

        List<Map<String, Object>> queryResultRows = jdbcTemplate.queryForList(sqlQuery);
        queryResultTable.setRows(queryResultRows);

        List<Column> columns = new ArrayList<>();
        queryResultRows.stream().findFirst().ifPresent(row -> row.forEach((columnKey, columnValue) -> {
            Column column = new Column();
            column.setColumnName(columnKey);

            columns.add(column);
        }));
        queryResultTable.setColumns(columns);

        tables.addAll(getTables());

        model.addAttribute(
                TABLE_VIEW_MODEL_NAME,
                tables
        );

        return TABLE_VIEW_FRAGMENT_NAME;
    }

    @DeleteMapping("/{tableName}")
    @HxRequest
    public String dropTable(@PathVariable String tableName, Model model) {
        List<Table> availableTables = getTables();
        if (availableTables.stream().anyMatch(table -> table.getTableName().equals(tableName))) {
            jdbcTemplate.execute("DROP TABLE " + tableName);
        }

        model.addAttribute(
                TABLE_VIEW_MODEL_NAME,
                getTables()
        );

        return TABLE_VIEW_FRAGMENT_NAME;
    }

    @DeleteMapping("/{tableName}/truncate")
    @HxRequest
    public String truncateTable(@PathVariable String tableName, Model model) {
        List<Table> availableTables = getTables();
        if (availableTables.stream().anyMatch(table -> table.getTableName().equals(tableName))) {
            jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
        }

        model.addAttribute(
                TABLE_VIEW_MODEL_NAME,
                getTables()
        );

        return TABLE_VIEW_FRAGMENT_NAME;
    }

    @ExceptionHandler(SQLException.class)
    @HxRequest
    public String error(SQLException sqlException, Model model) {
        model.addAttribute(
                "error",
                sqlException.getMessage()
        );

        model.addAttribute(
                TABLE_VIEW_MODEL_NAME,
                getTables()
        );

        return TABLE_VIEW_FRAGMENT_NAME;
    }

    private List<Table> getTables() {
        List<Table> tables = jdbcTemplate.query(
                "SELECT table_name FROM INFORMATION_SCHEMA.Tables WHERE lower(table_schema) = 'public' AND table_type = 'BASE TABLE'",
                new BeanPropertyRowMapper<>(Table.class)
        );
        tables.forEach(table -> {
            table.setColumns(
                    jdbcTemplate.query(
                            "SELECT column_name, data_type FROM INFORMATION_SCHEMA.Columns WHERE table_name = ?",
                            new BeanPropertyRowMapper<>(Column.class),
                            table.getTableName()
                    )
            );
            table.setRows(jdbcTemplate.queryForList("SELECT * FROM " + table.getTableName() + " FETCH FIRST 10 ROWS ONLY"));
        });

        return tables;
    }

    private String generateERDiagram() {
        String driverClassName = "";
        try {
            driverClassName = DriverManager.getDriver(dataSource.getConnection().getMetaData().getURL()).getClass().toString();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (driverClassName.equals("class org.postgresql.Driver")) {
            // See https://www.cybertec-postgresql.com/en/er-diagrams-with-sql-and-mermaid/#
            String sqlQuery = """
                SELECT 'erDiagram' AS mermaid_diagram_line
                UNION ALL
                SELECT
                    format(E'\\t%s {\\n%s\\n\\t}',
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
                    format('%s }|..|| %s : %s', c1.relname, c2.relname, c.conname)
                FROM
                    pg_constraint c
                    JOIN pg_class c1 ON c.conrelid = c1.oid AND c.contype = 'f'
                    JOIN pg_class c2 ON c.confrelid = c2.oid
                WHERE
                    NOT c1.relispartition AND NOT c2.relispartition;
            """;

            StringBuilder output = new StringBuilder();
            List<Map<String, Object>> queryResultRows = jdbcTemplate.queryForList(sqlQuery);
            for (Map<String, Object> queryResultRow : queryResultRows) {
                output.append(queryResultRow.get(ER_DIAGRAM_RESULT_SET_COLUMN_NAME));
            }

            return output.toString();
        }

        // Empty diagram
        return "erDiagram";
    }
}
