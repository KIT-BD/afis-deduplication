package com.neurotec.samples.server.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class DataMigrationManager {

    public void runMigration() throws SQLException {
        // Step 1: Save max(id)
        long maxId = getMaxId();
        System.out.println("MAX ID FROM QUERY "+ maxId);
        PropertyLoader.setTableMaxId(maxId);
        System.out.println("Max ID from table query is: " + maxId + ". Storing it for post-enrollment cleanup.");

        // Step 2: Truncate table
        truncateTable();
        System.out.println("Truncated table: " + PropertyLoader.getTable());

        // Step 3: Copy data from prod to default
        copyProdDataToDefault();
        System.out.println("Copied data from prod to default table.");

        deleteOldEntries();
        System.out.println("Deleted entries with ID <= " + maxId + " from the table.");
    }

    private Connection getDefaultConnection() throws SQLException {
        return DriverManager.getConnection(PropertyLoader.getDSN(), PropertyLoader.getUser(), PropertyLoader.getPassword());
    }

    private Connection getProdConnection() throws SQLException {
        return DriverManager.getConnection(PropertyLoader.getProdDSN(), PropertyLoader.getProdUser(), PropertyLoader.getProdPassword());
    }

    private long getMaxId() throws SQLException {
        long maxId = 0;
        String sql = "SELECT MAX(Id) FROM " + PropertyLoader.getTable();
        try (Connection conn = getDefaultConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                maxId = rs.getLong(1);
            }
        }
        return maxId;
    }

    private void truncateTable() throws SQLException {
        String sql = "TRUNCATE TABLE " + PropertyLoader.getTable();
        try (Connection conn = getDefaultConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private void copyProdDataToDefault() throws SQLException {
        String selectSql = "SELECT * FROM " + PropertyLoader.getProdTable();

        try (Connection prodConn = getProdConnection();
             Connection defaultConn = getDefaultConnection();
             Statement selectStmt = prodConn.createStatement();
             ResultSet rs = selectStmt.executeQuery(selectSql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            StringBuilder columnNames = new StringBuilder();
            StringBuilder valuePlaceholders = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.append(metaData.getColumnName(i));
                valuePlaceholders.append("?");
                if (i < columnCount) {
                    columnNames.append(", ");
                    valuePlaceholders.append(", ");
                }
            }

            String insertSql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                    PropertyLoader.getTable(), columnNames.toString(), valuePlaceholders.toString());

            try (PreparedStatement insertStmt = defaultConn.prepareStatement(insertSql)) {
                final int batchSize = 1000;
                int count = 0;

                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        int columnType = metaData.getColumnType(i);

                        if (columnType == java.sql.Types.BINARY ||
                                columnType == java.sql.Types.VARBINARY ||
                                columnType == java.sql.Types.LONGVARBINARY) {

                            insertStmt.setBytes(i, rs.getBytes(i));
                        } else {
                            insertStmt.setObject(i, rs.getObject(i));
                        }
                    }

                    insertStmt.addBatch();
                    count++;

                    if (count % batchSize == 0) {
                        insertStmt.executeBatch();
                        System.out.println("Inserted batch of " + batchSize);
                    }
                }

                insertStmt.executeBatch(); // insert remaining records
                System.out.println("Finished copying data. Total rows: " + count);
            }
        }
    }

    public void deleteOldEntries() throws SQLException {
        long maxId = PropertyLoader.getTableMaxId();
        if (maxId <= 0) {
            System.out.println("No old entries to delete (maxId from properties is " + maxId + ").");
            return;
        }

        String sql = "DELETE FROM " + PropertyLoader.getTable() + " WHERE Id <= ?";

        try (Connection conn = getDefaultConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, maxId);
            int deletedRows = stmt.executeUpdate();
            System.out.println("Deleted " + deletedRows + " old entries with ID <= " + maxId + " (using stored max ID from before migration).");
        }
    }
} 