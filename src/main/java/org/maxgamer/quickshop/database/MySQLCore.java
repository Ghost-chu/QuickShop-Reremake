/*
 * This file is a part of project QuickShop, the name is MySQLCore.java
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

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MySQLCore extends AbstractDatabaseCore {

    private static final int MAX_CONNECTIONS = 8;
    private final List<DatabaseConnection> POOL = new ArrayList<>();
    /**
     * The connection properties... user, pass, autoReconnect..
     */
    @NotNull
    private final Properties info;

    @NotNull
    private final String url;

    @NotNull
    private final QuickShop plugin;

    public MySQLCore(
            @NotNull QuickShop plugin,
            @NotNull String host,
            @NotNull String user,
            @NotNull String pass,
            @NotNull String database,
            @NotNull String port,
            boolean useSSL) {
        this.plugin = plugin;
        info = new Properties();
        info.setProperty("autoReconnect", "true");
        info.setProperty("user", user);
        info.setProperty("password", pass);
        info.setProperty("useUnicode", "true");
        info.setProperty("characterEncoding", "utf8");
        //info.setProperty("maxReconnects", "65535");
        // info.setProperty("failOverReadOnly", "false");
        info.setProperty("useSSL", String.valueOf(useSSL));
        if (false) { //TODO Option for addBatch to improve performance
            info.setProperty("rewriteBatchedStatements", "true");
        }
        this.url = "jdbc:mysql://" + host + ":" + port + "/" + database;
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            POOL.add(null);
        }
    }


    @Override
    synchronized void close() {
        for (DatabaseConnection databaseConnection : POOL) {
            if (databaseConnection == null || !databaseConnection.isValid()) {
                continue;
            }
            if (!databaseConnection.isUsing()) {
                databaseConnection.close();
            } else {
                //Wait until the connection is finished
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                } finally {
                    close();
                }
            }
        }
    }

    @Override
    synchronized protected DatabaseConnection getConnection0() {
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            DatabaseConnection connection = POOL.get(i);
            // If we have a current connection, fetch it
            if (connection == null) {
                return genConnection(i);
            } else if (!connection.isUsing()) {
                if (connection.isValid()) {
                    return connection;
                } else {
                    // Else, it is invalid, return another connection.
                    connection.close();
                    return genConnection(i);
                }
            }

        }
        //If all connection is unusable, wait a moment
        waitForConnection();
        return getConnection0();
    }

    synchronized private DatabaseConnection genConnection(int index) {
        try {
            DatabaseConnection connection = new DatabaseConnection(this, DriverManager.getConnection(this.url, info));
            POOL.set(index, connection);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to create a new connection", e);
        }
    }

    @Override
    public @NotNull String getName() {
        return "BuiltIn-MySQL";
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

}
