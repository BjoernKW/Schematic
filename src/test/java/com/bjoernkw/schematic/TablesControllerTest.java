package com.bjoernkw.schematic;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;

@WebMvcTest(TablesController.class)
@TestPropertySource(properties = {
        "schematic.path=schematic",
        "schematic.root-path=/"
})
class TablesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JdbcClient jdbcClient;

    @MockitoBean
    private DataSource dataSource;
    
    @MockitoBean(name = "schematicProperties")
    private SchematicProperties schematicProperties;

    private List<Table> mockTables;

    @BeforeEach
    void setUp() {
        mockTables = createMockTables();
        // Provide values for Thymeleaf bean expressions used in templates
        when(schematicProperties.getPath()).thenReturn("schematic");
    }

    @Test
    void queryDatabase_WithValidSQL_ShouldReturnTablesFragment() throws Exception {
        String sqlQuery = "SELECT * FROM users";
        List<Map<String, Object>> queryResults = createMockQueryResults();

        JdbcClient.StatementSpec anyStmt = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtTables = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtColumns = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtRows = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);

        when(jdbcClient.sql(anyString())).thenReturn(anyStmt);
        when(anyStmt.param(any())).thenReturn(anyStmt);

        // For direct query SQL
        when(jdbcClient.sql(eq(sqlQuery))).thenReturn(anyStmt);
        when(anyStmt.query().listOfRows()).thenReturn(queryResults);

        // For tables list
        when(jdbcClient.sql(contains("FROM INFORMATION_SCHEMA.Tables"))).thenReturn(stmtTables);
        when(stmtTables.query(any(BeanPropertyRowMapper.class)).list()).thenReturn(mockTables);

        // For columns per table
        when(jdbcClient.sql(contains("FROM INFORMATION_SCHEMA.Columns"))).thenReturn(stmtColumns);
        when(stmtColumns.param(any()).query(any(BeanPropertyRowMapper.class)).list()).thenReturn(createMockColumns());

        // For preview rows of each table
        when(jdbcClient.sql(contains("SELECT * FROM "))).thenReturn(stmtRows);
        when(stmtRows.query().listOfRows()).thenReturn(createMockRows());

        mockMvc.perform(get("/schematic/tables")
                .param("sqlQuery", sqlQuery)
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/tables"))
                .andExpect(model().attributeExists("tables"));

        verify(jdbcClient).sql(sqlQuery);
    }

    @Test
    void dropTable_WithExistingTable_ShouldDropTableAndReturnFragment() throws Exception {
        String tableName = "test_table";
        
        JdbcClient.StatementSpec stmtTables = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtColumns = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtRows = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtDrop = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);

        when(jdbcClient.sql(contains("FROM INFORMATION_SCHEMA.Tables"))).thenReturn(stmtTables);
        when(stmtTables.query(any(BeanPropertyRowMapper.class)).list()).thenReturn(mockTables);

        when(jdbcClient.sql(contains("FROM INFORMATION_SCHEMA.Columns"))).thenReturn(stmtColumns);
        when(stmtColumns.param(any()).query(any(BeanPropertyRowMapper.class)).list()).thenReturn(createMockColumns());

        when(jdbcClient.sql(contains("SELECT * FROM "))).thenReturn(stmtRows);
        when(stmtRows.query().listOfRows()).thenReturn(createMockRows());

        when(jdbcClient.sql("DROP TABLE " + tableName)).thenReturn(stmtDrop);

        mockMvc.perform(delete("/schematic/tables/" + tableName)
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/tables"))
                .andExpect(model().attributeExists("tables"));

        verify(stmtDrop).update();
    }

    @Test
    void dropTable_WithNonExistentTable_ShouldNotExecuteDrop() throws Exception {
        String tableName = "nonexistent_table";
        
        JdbcClient.StatementSpec stmtTables = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtColumns = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtRows = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtDrop = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);

        when(jdbcClient.sql(contains("FROM INFORMATION_SCHEMA.Tables"))).thenReturn(stmtTables);
        when(stmtTables.query(any(BeanPropertyRowMapper.class)).list()).thenReturn(List.of());

        when(jdbcClient.sql(contains("FROM INFORMATION_SCHEMA.Columns"))).thenReturn(stmtColumns);
        when(stmtColumns.param(any()).query(any(BeanPropertyRowMapper.class)).list()).thenReturn(createMockColumns());

        when(jdbcClient.sql(contains("SELECT * FROM "))).thenReturn(stmtRows);
        when(stmtRows.query().listOfRows()).thenReturn(createMockRows());

        mockMvc.perform(delete("/schematic/tables/" + tableName)
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/tables"));

        verify(stmtDrop, never()).update();
    }

    @Test
    void truncateTable_WithExistingTable_ShouldTruncateTableAndReturnFragment() throws Exception {
        String tableName = "test_table";
        
        JdbcClient.StatementSpec stmtTables = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtColumns = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtRows = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtTruncate = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);

        when(jdbcClient.sql(contains("FROM INFORMATION_SCHEMA.Tables"))).thenReturn(stmtTables);
        when(stmtTables.query(any(BeanPropertyRowMapper.class)).list()).thenReturn(mockTables);

        when(jdbcClient.sql(contains("FROM INFORMATION_SCHEMA.Columns"))).thenReturn(stmtColumns);
        when(stmtColumns.param(any()).query(any(BeanPropertyRowMapper.class)).list()).thenReturn(createMockColumns());

        when(jdbcClient.sql(contains("SELECT * FROM "))).thenReturn(stmtRows);
        when(stmtRows.query().listOfRows()).thenReturn(createMockRows());

        when(jdbcClient.sql("TRUNCATE TABLE " + tableName)).thenReturn(stmtTruncate);

        mockMvc.perform(delete("/schematic/tables/" + tableName + "/truncate")
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/tables"))
                .andExpect(model().attributeExists("tables"));

        verify(stmtTruncate).update();
    }

    @Test
    void truncateTable_WithNonExistentTable_ShouldNotExecuteTruncate() throws Exception {
        String tableName = "nonexistent_table";
        
        JdbcClient.StatementSpec stmtTables = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtColumns = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtRows = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtTruncate = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);

        when(jdbcClient.sql(contains("FROM INFORMATION_SCHEMA.Tables"))).thenReturn(stmtTables);
        when(stmtTables.query(any(BeanPropertyRowMapper.class)).list()).thenReturn(List.of());

        when(jdbcClient.sql(contains("FROM INFORMATION_SCHEMA.Columns"))).thenReturn(stmtColumns);
        when(stmtColumns.param(any()).query(any(BeanPropertyRowMapper.class)).list()).thenReturn(createMockColumns());

        when(jdbcClient.sql(contains("SELECT * FROM "))).thenReturn(stmtRows);
        when(stmtRows.query().listOfRows()).thenReturn(createMockRows());

        mockMvc.perform(delete("/schematic/tables/" + tableName + "/truncate")
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/tables"));

        verify(stmtTruncate, never()).update();
    }

    @Test
    void errorHandler_WithSQLException_ShouldReturnFragmentWithError() {
        SQLException sqlException = new SQLException("Database error");
        TablesController controller = new TablesController(jdbcClient, dataSource);
        Model model = mock(Model.class);

        // Stub JDBC interactions used by getTables() inside error handler
        JdbcClient.StatementSpec stmtTables = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtColumns = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);
        JdbcClient.StatementSpec stmtRows = mock(JdbcClient.StatementSpec.class, org.mockito.Mockito.RETURNS_DEEP_STUBS);

        when(jdbcClient.sql(org.mockito.ArgumentMatchers.contains("FROM INFORMATION_SCHEMA.Tables"))).thenReturn(stmtTables);
        when(stmtTables.query(any(BeanPropertyRowMapper.class)).list()).thenReturn(List.of());

        when(jdbcClient.sql(org.mockito.ArgumentMatchers.contains("FROM INFORMATION_SCHEMA.Columns"))).thenReturn(stmtColumns);
        when(stmtColumns.param(any()).query(any(BeanPropertyRowMapper.class)).list()).thenReturn(List.of());

        when(jdbcClient.sql(org.mockito.ArgumentMatchers.contains("SELECT * FROM "))).thenReturn(stmtRows);
        when(stmtRows.query().listOfRows()).thenReturn(List.of());

        String result = controller.error(sqlException, model);

        verify(model).addAttribute("error", "Database error");
        verify(model).addAttribute(eq("tables"), any());

        assert result.equals("fragments/tables");
    }

    private List<Table> createMockTables() {
        Table table = new Table();
        table.setTableName("test_table");
        table.setQueryResult(false);

        return List.of(table);
    }

    private List<Column> createMockColumns() {
        Column column = new Column();
        column.setColumnName("id");
        column.setDataType("INTEGER");

        return List.of(column);
    }

    private List<Map<String, Object>> createMockRows() {
        Map<String, Object> row = new HashMap<>();
        row.put("id", 1);
        row.put("name", "Test");

        return List.of(row);
    }

    private List<Map<String, Object>> createMockQueryResults() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", 1);
        result.put("username", "testuser");

        return List.of(result);
    }

}
