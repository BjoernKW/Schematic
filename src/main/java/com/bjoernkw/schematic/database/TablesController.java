package com.bjoernkw.schematic.database;

import io.github.wimdeblauwe.hsbt.mvc.HxRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/tables")
@RequiredArgsConstructor
public class TablesController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public String listTables(Model model) {
        model.addAttribute(
                "tables",
                getTables()
        );

        return "index";
    }

    @DeleteMapping("/{tableName}")
    @HxRequest
    public String dropTable(@PathVariable String tableName, Model model) {
        jdbcTemplate.execute("DROP TABLE " + tableName);

        model.addAttribute(
                "tables",
                getTables()
        );

        return "fragments/tables";
    }

    @DeleteMapping("/{tableName}/truncate")
    @HxRequest
    public String truncateTable(@PathVariable String tableName, Model model) {
        jdbcTemplate.execute("TRUNCATE TABLE " + tableName);

        model.addAttribute(
                "tables",
                getTables()
        );

        return "fragments/tables";
    }

    private List<Table> getTables() {
        List<Table> tables = jdbcTemplate.query(
                "SELECT table_name FROM INFORMATION_SCHEMA.Tables WHERE table_schema = 'public'",
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
