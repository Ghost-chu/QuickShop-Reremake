/*
 * This file is a part of project QuickShop, the name is DatabaseHelper.java
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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

/**
 * A Util to execute all SQLs.
 */
public class DatabaseHelper {

    @NotNull
    private final Database db;

    @NotNull
    private final QuickShop plugin;

    public DatabaseHelper(@NotNull QuickShop plugin, @NotNull Database db) throws SQLException {
        this.db = db;
        this.plugin = plugin;
        if (!db.hasTable(QuickShop.instance.getDbPrefix() + "shops")) {
            createShopsTable();
        }
        if (!db.hasTable(QuickShop.instance.getDbPrefix() + "messages")) {
            createMessagesTable();
        }
        checkColumns();
    }

    /**
     * Creates the database table 'shops'.
     */
    private void createShopsTable() {
        plugin.getDatabaseManager().add(() -> {
            try {
                Statement st = db.getConnection().createStatement();
                String createTable = "CREATE TABLE " + QuickShop.instance
                    .getDbPrefix() + "shops (owner  VARCHAR(255) NOT NULL, price  double(32, 2) NOT NULL, itemConfig TEXT CHARSET utf8 NOT NULL, x  INTEGER(32) NOT NULL, y  INTEGER(32) NOT NULL, z  INTEGER(32) NOT NULL, world VARCHAR(32) NOT NULL, unlimited  boolean, type  boolean, PRIMARY KEY (x, y, z, world) );";
                st.execute(createTable);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Creates the database table 'messages'
     *
     * @return Create failed or successed.
     * @throws SQLException If the connection is invalid
     */
    private void createMessagesTable() throws SQLException {
        plugin.getDatabaseManager().add(() -> {
            try {
                Statement st = db.getConnection().createStatement();
                String createTable = "CREATE TABLE " + QuickShop.instance.getDbPrefix()
                    + "messages (owner  VARCHAR(255) NOT NULL, message  TEXT(25) NOT NULL, time  BIGINT(32) NOT NULL );";
                if (plugin.getDatabase().getCore() instanceof MySQLCore) {
                    createTable = "CREATE TABLE " + QuickShop.instance.getDbPrefix()
                        + "messages (owner  VARCHAR(255) NOT NULL, message  TEXT(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL , time  BIGINT(32) NOT NULL );";
                }
                st.execute(createTable);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Verifies that all required columns exist.
     */
    private void checkColumns() {
        plugin.getDatabaseManager().add(() -> {
            PreparedStatement ps;
            try {
                // V3.4.2
                ps = db.getConnection().prepareStatement("ALTER TABLE " + QuickShop.instance
                    .getDbPrefix() + "shops MODIFY COLUMN price double(32,2) NOT NULL AFTER owner");
                ps.execute();
                ps.close();
            } catch (SQLException e) {
                //ignore
            }
            try {
                // V3.4.3
                ps = db.getConnection().prepareStatement("ALTER TABLE " + QuickShop.instance
                    .getDbPrefix() + "messages MODIFY COLUMN time BIGINT(32) NOT NULL AFTER message");
                ps.execute();
                ps.close();
            } catch (SQLException e) {
                //ignore
            }
            if (QuickShop.instance.getDatabase().getCore() instanceof MySQLCore) {
                try {
                    ps = db.getConnection().prepareStatement("ALTER TABLE " + QuickShop.instance
                        .getDbPrefix() + "messages MODIFY COLUMN message text CHARACTER SET utf8mb4 NOT NULL AFTER owner");
                    ps.execute();
                    ps.close();
                } catch (SQLException e) {
                    //ignore
                }
            }
        });

    }

    public void cleanMessage(long weekAgo) {
        plugin.getDatabaseManager().add(() -> {
            try {
                //QuickShop.instance.getDB().execute("DELETE FROM " + QuickShop.instance
                //        .getDbPrefix() + "messages WHERE time < ?", weekAgo);
                String sqlString = "DELETE FROM " + QuickShop.instance
                    .getDbPrefix() + "messages WHERE time < ?";
                PreparedStatement ps = db.getConnection().prepareStatement(sqlString);
                ps.setLong(1, weekAgo);
                ps.execute();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        });
    }

    public void cleanMessageForPlayer(@NotNull UUID player) {
        plugin.getDatabaseManager().add(() -> {
            try {
                String sqlString = "DELETE FROM " + QuickShop.instance.getDbPrefix() + "messages WHERE owner = ?";
                PreparedStatement ps = db.getConnection().prepareStatement(sqlString);
                ps.setString(1, player.toString());
                ps.execute();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        });
    }

    public boolean createShop(@NotNull String owner, double price, @NotNull ItemStack item, int unlimited, int shopType, @NotNull String world, int x, int y, int z) {
        try {
            removeShop(x, y, z, world); //First purge old exist shop before create new shop.
            String sqlString = "INSERT INTO " + QuickShop.instance
                .getDbPrefix() + "shops (owner, price, itemConfig, x, y, z, world, unlimited, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            //QuickShop.instance.getDB().execute(q, owner, price, Util.serialize(item), x, y, z, world, unlimited, shopType);
            PreparedStatement ps = db.getConnection().prepareStatement(sqlString);
            ps.setString(1, owner);
            ps.setDouble(2, price);
            ps.setString(3, Util.serialize(item));
            ps.setInt(4, x);
            ps.setInt(5, y);
            ps.setInt(6, z);
            ps.setString(7, world);
            ps.setInt(8, unlimited);
            ps.setInt(9, shopType);
            return ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void removeShop(int x, int y, int z, @NotNull String worldName) throws SQLException {
//		db.getConnection().createStatement()
//				.executeUpdate("DELETE FROM " + QuickShop.instance.getDbPrefix() + "shops WHERE x = " + x + " AND y = " + y
//						+ " AND z = " + z + " AND world = \"" + worldName + "\""
//						+ (db.getCore() instanceof MySQLCore ? " LIMIT 1" : ""));
        plugin.getDatabaseManager().add(() -> {
                try {
                    String sqlString = "DELETE FROM " + QuickShop.instance
                        .getDbPrefix() + "shops WHERE x = ? AND y = ? AND z = ? AND world = ?" + (db.getCore() instanceof MySQLCore ?
                        " LIMIT 1" :
                        "");

                    PreparedStatement ps = db.getConnection().prepareStatement(sqlString);
                    ps.setInt(1, x);
                    ps.setInt(2, y);
                    ps.setInt(3, z);
                    ps.setString(4, worldName);
                    ps.execute();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        );
    }

    public ResultSet selectAllMessages() throws SQLException {
        Statement st = db.getConnection().createStatement();
        String selectAllShops = "SELECT * FROM " + QuickShop.instance.getDbPrefix() + "messages";
        return st.executeQuery(selectAllShops);
    }

    public ResultSet selectAllShops() throws SQLException {
        Statement st = db.getConnection().createStatement();
        String selectAllShops = "SELECT * FROM " + QuickShop.instance.getDbPrefix() + "shops";
        return st.executeQuery(selectAllShops);
    }

    public void sendMessage(@NotNull UUID player, @NotNull String message, long time) {
        plugin.getDatabaseManager().add(() -> {
            try {
                String sqlString = "INSERT INTO " + QuickShop.instance
                    .getDbPrefix() + "messages (owner, message, time) VALUES (?, ?, ?)";
                //QuickShop.instance.getDB().execute(q, player.toString(), message, System.currentTimeMillis());
                PreparedStatement ps = db.getConnection().prepareStatement(sqlString);
                ps.setString(1, player.toString());
                ps.setString(2, message);
                ps.setLong(3, time);
                ps.execute();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        });
    }

    public void updateOwner2UUID(@NotNull String ownerUUID, int x, int y, int z, @NotNull String worldName) {
        //QuickShop.instance.getDB().getConnection().createStatement()
        //         .executeUpdate("UPDATE " + QuickShop.instance.getDbPrefix() + "shops SET owner = \"" + ownerUUID.toString()
        //                 + "\" WHERE x = " + x + " AND y = " + y + " AND z = " + z
        //                 + " AND world = \"" + worldName + "\" LIMIT 1");
        plugin.getDatabaseManager().add(() -> {
            try {
                String sqlString = "UPDATE " + QuickShop.instance
                    .getDbPrefix() + "shops SET owner = ? WHERE x = ? AND y = ? AND z = ? AND world = ?" + (db
                    .getCore() instanceof MySQLCore ? " LIMIT 1" : "");
                PreparedStatement ps = db.getConnection().prepareStatement(sqlString);
                ps.setString(1, ownerUUID);
                ps.setInt(2, x);
                ps.setInt(3, y);
                ps.setInt(4, z);
                ps.setString(5, worldName);
                ps.execute();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        });
    }

    public void updateShop(@NotNull String owner, @NotNull ItemStack item, int unlimited, int shopType,
                           double price, int x, int y, int z, String world) {
        plugin.getDatabaseManager().add(() -> {
            try {
                String sqlString = "UPDATE " + QuickShop.instance
                    .getDbPrefix() + "shops SET owner = ?, itemConfig = ?, unlimited = ?, type = ?, price = ? WHERE x = ? AND y = ? and z = ? and world = ?";
                PreparedStatement ps = db.getConnection().prepareStatement(sqlString);
                ps.setString(1, owner);
                ps.setString(2, Util.serialize(item));
                ps.setInt(3, unlimited);
                ps.setInt(4, shopType);
                ps.setDouble(5, price);
                ps.setInt(6, x);
                ps.setInt(7, y);
                ps.setInt(8, z);
                ps.setString(9, world);
                ps.execute();
                //db.execute(q, owner, Util.serialize(item), unlimited, shopType, price, x, y, z, world);
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        });
    }

}