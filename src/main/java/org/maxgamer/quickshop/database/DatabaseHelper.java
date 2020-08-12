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

package org.maxgamer.quickshop.database;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.shop.ShopModerator;
import org.maxgamer.quickshop.util.Util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A Util to execute all SQLs.
 */
public class DatabaseHelper {

    @NotNull
    private final Database db;

    @NotNull
    private final DatabaseManager manager;

    @NotNull
    private final QuickShop plugin;

    public DatabaseHelper(@NotNull QuickShop plugin, @NotNull Database db, @NotNull DatabaseManager manager) throws SQLException {
        this.db = db;
        this.plugin = plugin;
        this.manager = manager;
        if (!db.hasTable(plugin.getDbPrefix() + "shops")) {
            createShopsTable();
        }
        if (!db.hasTable(plugin.getDbPrefix() + "messages")) {
            createMessagesTable();
        }
        checkColumns();
    }

    /**
     * Creates the database table 'shops'.
     *
     * @throws SQLException If the connection is invalid.
     */

    private void createShopsTable() throws SQLException {
         Statement st = db.getConnection().createStatement();
        String createTable = "CREATE TABLE " + plugin
                .getDbPrefix() + "shops (owner  VARCHAR(255) NOT NULL, price  double(32, 2) NOT NULL, itemConfig TEXT CHARSET utf8 NOT NULL, x  INTEGER(32) NOT NULL, y  INTEGER(32) NOT NULL, z  INTEGER(32) NOT NULL, world VARCHAR(32) NOT NULL, unlimited  boolean, type  boolean, PRIMARY KEY (x, y, z, world) );";
        st.execute(createTable);
    }

    /**
     * Creates the database table 'messages'
     *
     * @return Create failed or successed.
     * @throws SQLException If the connection is invalid
     */
    private boolean createMessagesTable() throws SQLException {
        Statement st = db.getConnection().createStatement();
        String createTable = "CREATE TABLE " + plugin.getDbPrefix()
                + "messages (owner  VARCHAR(255) NOT NULL, message  TEXT(25) NOT NULL, time  BIGINT(32) NOT NULL );";
        if (plugin.getDatabase().getCore() instanceof MySQLCore) {
            createTable = "CREATE TABLE " + plugin.getDbPrefix()
                    + "messages (owner  VARCHAR(255) NOT NULL, message  TEXT(25) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL , time  BIGINT(32) NOT NULL );";
        }
        return st.execute(createTable);
    }

    /**
     * Verifies that all required columns exist.
     */
    private void checkColumns() {
        PreparedStatement ps = null;
        try {
            // V3.4.2

            ps = db.getConnection().prepareStatement("ALTER TABLE " + plugin
                    .getDbPrefix() + "shops MODIFY COLUMN price double(32,2) NOT NULL AFTER owner");
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            //ignore
        }
        try {
            // V3.4.3
            ps = db.getConnection().prepareStatement("ALTER TABLE " + plugin
                    .getDbPrefix() + "messages MODIFY COLUMN time BIGINT(32) NOT NULL AFTER message");
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            //ignore
        }
        try {
            if (!db.hasColumn(plugin
                    .getDbPrefix() + "shops", "extra")) {
                // Reremake - DataStorage @TODO needs testing
                if (db.getCore() instanceof MySQLCore) {
                    ps = db.getConnection().prepareStatement("ALTER TABLE " + plugin
                            .getDbPrefix() + "shops ADD extra LONGTEXT");
                } else {
                    ps = db.getConnection().prepareStatement("ALTER TABLE " + plugin
                            .getDbPrefix() + "shops ADD COLUMN extra TEXT");
                }
                Util.debugLog("Setting up the column EXTRA...");
                ps.execute();
                ps.close();
            }
        } catch (SQLException e) {
            //ignore
            Util.debugLog("Error to create EXTRA column: " + e.getMessage());
        }
        if (db.getCore() instanceof MySQLCore) {
            try {
                ps = db.getConnection().prepareStatement("ALTER TABLE " + plugin
                        .getDbPrefix() + "messages MODIFY COLUMN message text CHARACTER SET utf8mb4 NOT NULL AFTER owner");
                ps.execute();
                ps.close();
            } catch (SQLException e) {
                //ignore
            }
        }

        if (ps != null) {
            try {
                ps.close();
            } catch (SQLException ignored) {
            }
        }

    }

    public void cleanMessage(long weekAgo) {
        //plugin.getDB().execute("DELETE FROM " + plugin
        //        .getDbPrefix() + "messages WHERE time < ?", weekAgo);
        String sqlString = "DELETE FROM " + plugin
                .getDbPrefix() + "messages WHERE time < ?";
        manager.add(new DatabaseTask(sqlString, ps -> ps.setLong(1, weekAgo)));
    }

    public void cleanMessageForPlayer(@NotNull UUID player) {
        String sqlString = "DELETE FROM " + plugin.getDbPrefix() + "messages WHERE owner = ?";
        manager.add(new DatabaseTask(sqlString, (ps) -> ps.setString(1, player.toString())));
    }

    public void createShop(@NotNull Shop shop, @Nullable Runnable onSuccess, @Nullable Consumer<SQLException> onFailed) {
        removeShop(shop); //First purge old exist shop before create new shop.
        String sqlString = "INSERT INTO " + plugin.getDbPrefix() + "shops (owner, price, itemConfig, x, y, z, world, unlimited, type, extra) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        manager.add(new DatabaseTask(sqlString, new DatabaseTask.Task() {
            @Override
            public void edit(PreparedStatement ps) throws SQLException {
                Location location = shop.getLocation();
                //plugin.getDB().execute(q, owner, price, Util.serialize(item), x, y, z, world, unlimited, shopType);
                ps.setString(1, ShopModerator.serialize(shop.getModerator()));
                ps.setDouble(2, shop.getPrice());
                ps.setString(3, Util.serialize(shop.getItem()));
                ps.setInt(4, location.getBlockX());
                ps.setInt(5, location.getBlockY());
                ps.setInt(6, location.getBlockZ());
                String worldName = "undefined";
                if (location.getWorld() != null) {
                    worldName = location.getWorld().getName();
                } else {
                    plugin.getLogger().warning("Warning: Shop " + shop + " had null world name due we will save it as undefined world to trying keep data.");
                }
                ps.setString(7, worldName);
                ps.setInt(8, shop.isUnlimited() ? 1 : 0);
                ps.setInt(9, shop.getShopType().toID());
                ps.setString(10, shop.saveExtraToJson());
            }

            @Override
            public void onSuccess() {
                if (!shop.isDeleted() && onSuccess != null) {
                    onSuccess.run();
                }
            }

            @Override
            public void onFailed(SQLException e) {
                if (onFailed != null) {
                    onFailed.accept(e);
                } else {
                    e.printStackTrace();
                    plugin.getLogger().warning("Warning: Shop " + shop.toString() + " failed save to database, the shop may disappear after plugin reload or server restart!");
                }
            }
        }));
    }

    public void removeShop(Shop shop) {
        plugin.log("[DATABASE HELPER] Removing shop in the database: " + shop.toString());
        if (plugin.getConfig().getBoolean("debug.shop-deletion")) {
            for (StackTraceElement stackTraceElement : new Exception().getStackTrace()) {
                plugin.log("at [" + stackTraceElement.getClassName() + "] [" + stackTraceElement.getMethodName() + "] (" + stackTraceElement.getLineNumber() + ") - " + stackTraceElement.getFileName());
            }
        }
        //TODO: Trace the delete from
//		db.getConnection().createStatement()
//				.executeUpdate("DELETE FROM " + plugin.getDbPrefix() + "shops WHERE x = " + x + " AND y = " + y
//						+ " AND z = " + z + " AND world = \"" + worldName + "\""
//						+ (db.getCore() instanceof MySQLCore ? " LIMIT 1" : ""));
        String sqlString = "DELETE FROM "
                + plugin.getDbPrefix()
                + "shops WHERE x = ? AND y = ? AND z = ? AND world = ?"
                + (db.getCore() instanceof MySQLCore ? " LIMIT 1" : "");
        manager.add(new DatabaseTask(sqlString, (ps) -> {
            Location location = shop.getLocation();
            ps.setInt(1, location.getBlockX());
            ps.setInt(2, location.getBlockY());
            ps.setInt(3, location.getBlockZ());
            ps.setString(4, location.getWorld().getName());
        }));

    }

    public ResultSet selectAllMessages() throws SQLException {
        Statement st = db.getConnection().createStatement();
        String selectAllShops = "SELECT * FROM " + plugin.getDbPrefix() + "messages";
        return st.executeQuery(selectAllShops);
    }

    public ResultSet selectAllShops() throws SQLException {
        Statement st = db.getConnection().createStatement();
        String selectAllShops = "SELECT * FROM " + plugin.getDbPrefix() + "shops";
        return st.executeQuery(selectAllShops);
    }

    public void sendMessage(@NotNull UUID player, @NotNull String message, long time) {

        String sqlString = "INSERT INTO " + plugin.getDbPrefix() + "messages (owner, message, time) VALUES (?, ?, ?)";
        manager.add(
                new DatabaseTask(
                        sqlString,
                        (ps) -> {
                            ps.setString(1, player.toString());
                            ps.setString(2, message);
                            ps.setLong(3, time);
                        }));
    }

    public void updateOwner2UUID(@NotNull String ownerUUID, int x, int y, int z, @NotNull String worldName) {
        String sqlString = "UPDATE " + plugin
                .getDbPrefix() + "shops SET owner = ? WHERE x = ? AND y = ? AND z = ? AND world = ?" + (db
                .getCore() instanceof MySQLCore ? " LIMIT 1" : "");
        manager.add(
                //plugin.getDB().getConnection().createStatement()
                //         .executeUpdate("UPDATE " + plugin.getDbPrefix() + "shops SET owner = \"" + ownerUUID.toString()
                //                 + "\" WHERE x = " + x + " AND y = " + y + " AND z = " + z
                //                 + " AND world = \"" + worldName + "\" LIMIT 1");
                new DatabaseTask(sqlString, ps -> {
                    ps.setString(1, ownerUUID);
                    ps.setInt(2, x);
                    ps.setInt(3, y);
                    ps.setInt(4, z);
                    ps.setString(5, worldName);
                }));
    }

    public void updateShop(@NotNull String owner, @NotNull ItemStack item, int unlimited, int shopType,
                           double price, int x, int y, int z, String world, String extra) {
        String sqlString = "UPDATE " + plugin
                .getDbPrefix() + "shops SET owner = ?, itemConfig = ?, unlimited = ?, type = ?, price = ?, extra = ? WHERE x = ? AND y = ? and z = ? and world = ?";
        manager.add(new DatabaseTask(sqlString, ps -> {
            ps.setString(1, owner);
            ps.setString(2, Util.serialize(item));
            ps.setInt(3, unlimited);
            ps.setInt(4, shopType);
            ps.setDouble(5, price);
            ps.setString(6, extra);
            ps.setInt(7, x);
            ps.setInt(8, y);
            ps.setInt(9, z);
            ps.setString(10, world);
        }));
        //db.execute(q, owner, Util.serialize(item), unlimited, shopType, price, x, y, z, world);

    }

}