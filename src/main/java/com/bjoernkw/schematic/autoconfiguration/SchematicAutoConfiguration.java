package com.bjoernkw.schematic.autoconfiguration;

import com.bjoernkw.schematic.SchematicProperties;
import com.bjoernkw.schematic.TablesController;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@AutoConfiguration
@ConditionalOnWebApplication
public class SchematicAutoConfiguration {

    private final JdbcTemplate jdbcTemplate;

    private final DataSource dataSource;

    public SchematicAutoConfiguration(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public TablesController tablesController() {
        return new TablesController(jdbcTemplate, dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public SchematicProperties schematicProperties() {
        return new SchematicProperties();
    }
}
