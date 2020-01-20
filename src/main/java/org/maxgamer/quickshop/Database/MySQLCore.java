/*
 * This file is a part of project QuickShop, the name is MySQLCore.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MySQLCore implements DatabaseCore {

  private static final ArrayList<Connection> POOL = new ArrayList<>();

  private static final int MAX_CONNECTIONS = 8;

  /** The connection properties... user, pass, autoReconnect.. */
  @NotNull private final Properties info;

  @NotNull private final String url;

  public MySQLCore(
      @NotNull String host,
      @NotNull String user,
      @NotNull String pass,
      @NotNull String database,
      @NotNull String port,
      boolean useSSL) {
    info = new Properties();
    info.setProperty("autoReconnect", "true");
    info.setProperty("user", user);
    info.setProperty("password", pass);
    info.setProperty("useUnicode", "true");
    info.setProperty("characterEncoding", "utf8");
    info.setProperty("useSSL", String.valueOf(useSSL));
    this.url = "jdbc:mysql://" + host + ":" + port + "/" + database;
    for (int i = 0; i < MAX_CONNECTIONS; i++) {
      POOL.add(null);
    }
  }

  @Override
  public void close() {
    // Nothing, because queries are executed immediately for MySQL
  }

  @Override
  public void flush() {
    // Nothing, because queries are executed immediately for MySQL
  }

  @Override
  public void queue(@NotNull BufferStatement bs) {
    try {
      Connection con = this.getConnection();
      while (con == null) {
        try {
          Thread.sleep(15);
        } catch (InterruptedException e) {
          // ignore
        }
        // Try again
        con = this.getConnection();
      }
      PreparedStatement ps = bs.prepareStatement(con);
      ps.execute();
      ps.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets the database connection for executing queries on.
   *
   * @return The database connection
   */
  @Nullable
  @Override
  public Connection getConnection() {
    for (int i = 0; i < MAX_CONNECTIONS; i++) {
      Connection connection = POOL.get(i);
      try {
        // If we have a current connection, fetch it
        if (connection != null && !connection.isClosed()) {
          if (connection.isValid(10)) {
            return connection;
          }
          // Else, it is invalid, so we return another connection.
        }
        connection = DriverManager.getConnection(this.url, info);

        POOL.set(i, connection);
        return connection;
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return null;
  }
}
