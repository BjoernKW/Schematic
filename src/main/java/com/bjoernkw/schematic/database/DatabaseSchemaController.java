package com.bjoernkw.schematic.database;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class DatabaseSchemaController {

    private final JdbcTemplate jdbcTemplate;
}
