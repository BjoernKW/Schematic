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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private DataSource dataSource;
    
    @MockitoBean
    private SchematicProperties schematicProperties;

    private List<Table> mockTables;

    @BeforeEach
    void setUp() {
        mockTables = createMockTables();
    }

    @Test
    void queryDatabase_WithValidSQL_ShouldReturnTablesFragment() throws Exception {
        String sqlQuery = "SELECT * FROM users";
        List<Map<String, Object>> queryResults = createMockQueryResults();
        
        when(jdbcTemplate.queryForList(sqlQuery)).thenReturn(queryResults);
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class)))
                .thenReturn(mockTables);
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class), anyString()))
                .thenReturn(createMockColumns());
        when(jdbcTemplate.queryForList(contains("SELECT * FROM")))
                .thenReturn(createMockRows());

        mockMvc.perform(get("/schematic/tables")
                .param("sqlQuery", sqlQuery)
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/tables"))
                .andExpect(model().attributeExists("tables"));

        verify(jdbcTemplate).queryForList(sqlQuery);
    }

    @Test
    void dropTable_WithExistingTable_ShouldDropTableAndReturnFragment() throws Exception {
        String tableName = "test_table";
        
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class)))
                .thenReturn(mockTables);
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class), anyString()))
                .thenReturn(createMockColumns());
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(createMockRows());

        mockMvc.perform(delete("/schematic/tables/" + tableName)
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/tables"))
                .andExpect(model().attributeExists("tables"));

        verify(jdbcTemplate).execute("DROP TABLE " + tableName);
    }

    @Test
    void dropTable_WithNonExistentTable_ShouldNotExecuteDrop() throws Exception {
        String tableName = "nonexistent_table";
        
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class)))
                .thenReturn(List.of()); // Empty list - no tables
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class), anyString()))
                .thenReturn(createMockColumns());
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(createMockRows());

        mockMvc.perform(delete("/schematic/tables/" + tableName)
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/tables"));

        verify(jdbcTemplate, never()).execute("DROP TABLE " + tableName);
    }

    @Test
    void truncateTable_WithExistingTable_ShouldTruncateTableAndReturnFragment() throws Exception {
        String tableName = "test_table";
        
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class)))
                .thenReturn(mockTables);
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class), anyString()))
                .thenReturn(createMockColumns());
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(createMockRows());

        mockMvc.perform(delete("/schematic/tables/" + tableName + "/truncate")
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/tables"))
                .andExpect(model().attributeExists("tables"));

        verify(jdbcTemplate).execute("TRUNCATE TABLE " + tableName);
    }

    @Test
    void truncateTable_WithNonExistentTable_ShouldNotExecuteTruncate() throws Exception {
        String tableName = "nonexistent_table";
        
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class)))
                .thenReturn(List.of()); // Empty list - no tables
        when(jdbcTemplate.query(anyString(), any(BeanPropertyRowMapper.class), anyString()))
                .thenReturn(createMockColumns());
        when(jdbcTemplate.queryForList(anyString()))
                .thenReturn(createMockRows());

        mockMvc.perform(delete("/schematic/tables/" + tableName + "/truncate")
                .header("HX-Request", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("fragments/tables"));

        verify(jdbcTemplate, never()).execute("TRUNCATE TABLE " + tableName);
    }

    @Test
    void errorHandler_WithSQLException_ShouldReturnFragmentWithError() {
        SQLException sqlException = new SQLException("Database error");
        TablesController controller = new TablesController(jdbcTemplate, dataSource);
        Model model = mock(Model.class);

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
