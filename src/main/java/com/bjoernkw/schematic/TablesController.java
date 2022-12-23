package com.bjoernkw.schematic;

import io.github.wimdeblauwe.hsbt.mvc.HxRequest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/schematic/tables")
public class TablesController {

    private static final String TABLE_VIEW_MODEL_NAME = "tables";

    private static final String TABLE_VIEW_FRAGMENT_NAME = "fragments/tables";

    private final JdbcTemplate jdbcTemplate;

    public TablesController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public String listTables(Model model) {
        model.addAttribute(
                TABLE_VIEW_MODEL_NAME,
                getTables()
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
                "SELECT table_name FROM INFORMATION_SCHEMA.Tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'",
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
}
