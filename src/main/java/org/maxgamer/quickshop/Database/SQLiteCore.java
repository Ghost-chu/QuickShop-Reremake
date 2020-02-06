/*
 * This file is a part of project QuickShop, the name is SQLiteCore.java
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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SQLiteCore implements DatabaseCore {
  private final File dbFile;
  private final LinkedList<BufferStatement> queue = new LinkedList<>();
  private Connection connection;
  private volatile Thread watcher;

  public SQLiteCore(@NotNull File dbFile) {
    this.dbFile = dbFile;
  }

  @Override
  public void close() {
    flush();
  }

  @Override
  public void flush() {
    while (!queue.isEmpty()) {
      BufferStatement bs;
      synchronized (queue) {
        bs = queue.removeFirst();
      }
      synchronized (dbFile) {
        try {
          PreparedStatement ps = bs.prepareStatement(Objects.requireNonNull(getConnection()));
          ps.execute();
          ps.close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public void queue(@NotNull BufferStatement bs) {
    synchronized (queue) {
      queue.add(bs);
    }
    if (watcher == null || !watcher.isAlive()) {
      startWatcher();
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
    try {
      // If we have a current connection, fetch it
      if (this.connection != null && !this.connection.isClosed()) {
        return this.connection;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    if (this.dbFile.exists()) {
      // So we need a new connection
      try {
        Class.forName("org.sqlite.JDBC");
        this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbFile);
        return this.connection;
      } catch (ClassNotFoundException | SQLException e) {
        e.printStackTrace();
        return null;
      }
    } else {
      // So we need a new file too.
      try {
        // Create the file
        this.dbFile.createNewFile();
        // Now we won't need a new file, just a connection.
        // This will return that new connection.
        return this.getConnection();
      } catch (IOException e) {
        e.printStackTrace();
        return null;
      }
    }
  }

  private void startWatcher() {
    watcher =
        new Thread(
            () -> {
              try {
                Thread.sleep(30000);
              } catch (InterruptedException e) {
                // ignore
              }
              flush();
            });
    watcher.start();
  }
}
