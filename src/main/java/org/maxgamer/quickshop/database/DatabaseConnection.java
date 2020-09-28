package org.maxgamer.quickshop.database;

import org.maxgamer.quickshop.QuickShop;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection implements AutoCloseable {

    private final Connection connection;
    private volatile boolean using;

    public DatabaseConnection(Connection connection) {
        this.connection = connection;
    }

    public boolean isValid() {
        try {
            return connection.isValid(30000);
        } catch (SQLException ignored) {
            return false;
        } catch (AbstractMethodError ignored) {
            //driver not supported
            return true;
        }
    }

    public void close() {
        try {
            Connection connection = get();
            if (!connection.isClosed()) {
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
                connection.close();
            }
            release();
        } catch (SQLException ignored) {
        }
    }

    public Connection get() {
        if (!using) {
            using = true;
            return connection;
        } else {
            throw new ConnectionIsUsingException();
        }
    }

    public void release() {
        if (using) {
            using = false;
            QuickShop.getInstance().getDatabaseManager().getDatabase().signalForNewConnection();
        }
    }

    public boolean isUsing() {
        return using;
    }

    public static class ConnectionIsUsingException extends IllegalStateException {
    }
}
