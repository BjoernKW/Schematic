package com.bjoernkw.schematic.config;

import com.bjoernkw.schematic.database.TablesController;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@ConditionalOnClass(TablesController.class)
@RequiredArgsConstructor
public class SchematicAutoConfiguration {

    private final JdbcTemplate jdbcTemplate;

    @Bean
    @ConditionalOnMissingBean
    public TablesController tablesController() {
        return new TablesController(jdbcTemplate);
    }
}
