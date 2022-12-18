package com.bjoernkw.schematic;

import io.github.wimdeblauwe.hsbt.mvc.HxRequest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/tables")
public class TablesController {

    private static final String VIEW_MODEL_NAME = "tables";

    private final JdbcTemplate jdbcTemplate;

    public TablesController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public String listTables(Model model, CsrfToken csrfToken) {
        model.addAttribute(
                VIEW_MODEL_NAME,
                getTables()
        );
        model.addAttribute(
                "csrfTokenHeader",
                "{\"" + csrfToken.getHeaderName() + "\": \"" + csrfToken.getToken() + "\"}"
        );

        return "index";
    }

    @DeleteMapping("/{tableName}")
    @HxRequest
    public String dropTable(@PathVariable String tableName, Model model) {
        List<Table> availableTables = getTables();
        if (availableTables.stream().anyMatch(table -> table.getTableName().equals(tableName))) {
            jdbcTemplate.execute("DROP TABLE " + tableName);
        }

        model.addAttribute(
                VIEW_MODEL_NAME,
                getTables()
        );

        return "fragments/tables";
    }

    @DeleteMapping("/{tableName}/truncate")
    @HxRequest
    public String truncateTable(@PathVariable String tableName, Model model) {
        List<Table> availableTables = getTables();
        if (availableTables.stream().anyMatch(table -> table.getTableName().equals(tableName))) {
            jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
        }

        model.addAttribute(
                VIEW_MODEL_NAME,
                getTables()
        );

        return "fragments/tables";
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
            table.setEntries(jdbcTemplate.queryForList("SELECT * FROM " + table.getTableName()));
        });

        return tables;
    }
}
