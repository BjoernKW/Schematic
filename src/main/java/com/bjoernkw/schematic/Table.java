package com.bjoernkw.schematic;

import java.util.List;
import java.util.Map;

class Table {

    private String tableName;

    private List<Column> columns;

    private List<Map<String, Object>> rows;

    private Boolean isQueryResult;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public void setRows(List<Map<String, Object>> rows) {
        this.rows = rows;
    }

    public Boolean getQueryResult() {
        return isQueryResult;
    }

    public void setQueryResult(Boolean queryResult) {
        isQueryResult = queryResult;
    }
}
