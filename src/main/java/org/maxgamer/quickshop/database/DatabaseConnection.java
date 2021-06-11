/*
 * This file is a part of project QuickShop, the name is DatabaseConnection.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.database;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection implements AutoCloseable {

    private final Connection connection;
    private final AbstractDatabaseCore databaseCore;
    private volatile boolean using;

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

    @Override
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
