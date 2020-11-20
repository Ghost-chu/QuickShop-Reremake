package org.maxgamer.quickshop.database;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection implements AutoCloseable {

    private final Connection connection;
    private volatile boolean using;
    private final AbstractDatabaseCore databaseCore;

    public DatabaseConnection(AbstractDatabaseCore databaseCore, Connection connection) {
        this.databaseCore = databaseCore;
        this.connection = connection;
    }

    public synchronized boolean isValid() {
        try {
            return !connection.isClosed() && connection.isValid(8000);
        } catch (SQLException ignored) {
            return false;
        } catch (AbstractMethodError ignored) {
            //driver not supported
            return true;
        }
    }

    public synchronized void close() {
        try {
            markUsing();
            Connection connection = get();
            if (!connection.isClosed()) {
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
                connection.close();
            }
        } catch (SQLException ignored) {
        } finally {
            release();
        }
    }


    synchronized void markUsing() {
        if (!using) {
            using = true;
        } else {
            throw new ConnectionIsUsingException();
        }
    }

    public synchronized Connection get() {
        if (using) {
            return connection;
        } else {
            throw new ConnectionIsNotUsingException();
        }
    }

    public synchronized void release() {
        if (using) {
            using = false;
            databaseCore.signalForNewConnection();
        } else {
            throw new ConnectionIsNotUsingException();
        }
    }

    public synchronized boolean isUsing() {
        return using;
    }

    public static class ConnectionIsUsingException extends IllegalStateException {
    }

    public static class ConnectionIsNotUsingException extends IllegalStateException {
    }
}
