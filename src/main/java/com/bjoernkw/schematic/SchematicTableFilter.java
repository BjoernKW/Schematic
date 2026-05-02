package com.bjoernkw.schematic;

/**
 * Controls which tables are visible and which operations are permitted in the Schematic UI.
 * Register an implementation as a Spring bean to restrict access. When no bean is present,
 * all tables are visible and all operations are permitted.
 */
public interface SchematicTableFilter {

    default boolean isTableVisible(String tableName) {
        return true;
    }

    default boolean isOperationPermitted(String tableName, TableOperation operation) {
        return true;
    }
}
