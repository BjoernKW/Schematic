package com.bjoernkw.schematic.autoconfiguration;

import com.bjoernkw.schematic.SchematicProperties;
import com.bjoernkw.schematic.TablesController;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration
@ConditionalOnWebApplication
public class SchematicAutoConfiguration {

    private final JdbcTemplate jdbcTemplate;

    public SchematicAutoConfiguration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public TablesController tablesController() {
        return new TablesController(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public SchematicProperties schematicApplicationProperties() {
        return new SchematicProperties();
    }
}
