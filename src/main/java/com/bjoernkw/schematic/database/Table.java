package com.bjoernkw.schematic.database;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
class Table {

    String tableName;

    List<Column> columns;

    List<Map<String, Object>> entries;
}
