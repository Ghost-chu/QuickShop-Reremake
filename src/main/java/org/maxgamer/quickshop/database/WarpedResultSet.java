package org.maxgamer.quickshop.database;

import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WarpedResultSet implements AutoCloseable {
    @Getter
    private final ResultSet resultSet;
    private final DatabaseConnection databaseConnection;

    public WarpedResultSet(ResultSet resultSet, DatabaseConnection databaseConnection) {
        this.resultSet = resultSet;
        this.databaseConnection = databaseConnection;
    }

    @Override
    public void close() throws SQLException {
        resultSet.close();
        databaseConnection.release();
    }
}
