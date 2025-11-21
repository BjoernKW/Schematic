package com.bjoernkw.schematic.autoconfiguration;

import com.bjoernkw.schematic.SchematicProperties;
import com.bjoernkw.schematic.TablesController;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;

@AutoConfiguration
@ConditionalOnWebApplication
public class SchematicAutoConfiguration {

    private final JdbcClient jdbcClient;

    private final DataSource dataSource;

    public SchematicAutoConfiguration(JdbcClient jdbcClient, DataSource dataSource) {
        this.jdbcClient = jdbcClient;
        this.dataSource = dataSource;
    }

    @Bean
    @ConditionalOnMissingBean
    public TablesController tablesController() {
        return new TablesController(jdbcClient, dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public SchematicProperties schematicProperties() {
        return new SchematicProperties();
    }
}
