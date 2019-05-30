package org.maxgamer.quickshop.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

public class DatabaseHelper {
    private Database db;
    private QuickShop plugin;
    private Queue<PreparedStatement> sqlQueue = new LinkedBlockingQueue<>();

    public DatabaseHelper(QuickShop plugin, Database db) throws SQLException {
        if (!db.hasTable(QuickShop.instance.getDbPrefix() + "shops")) {
            createShopsTable(db);
        }
        if (!db.hasTable(QuickShop.instance.getDbPrefix() + "messages")) {
            createMessagesTable(db);
        }
        checkColumns(db);
        this.db = db;
        this.plugin = plugin;

    }

    /**
     * Verifies that all required columns exist.
     */
    private void checkColumns(Database db) {
        PreparedStatement ps = null;
        try {
            // V3.4.2
            ps = db.getConnection().prepareStatement("ALTER TABLE " + QuickShop.instance
                    .getDbPrefix() + "shops MODIFY COLUMN price double(32,2) NOT NULL AFTER owner");
            ps.execute();
            ps.close();
        } catch (SQLException e) {
        }
        try {
            // V3.4.3
            ps = db.getConnection().prepareStatement("ALTER TABLE " + QuickShop.instance
                    .getDbPrefix() + "messages MODIFY COLUMN time BIGINT(32) NOT NULL AFTER message");
            ps.execute();
            ps.close();
        } catch (SQLException e) {
        }
    }

    /**
     * Creates the database table 'shops'.
     *
     * @throws SQLException If the connection is invalid.
     */
    public void createShopsTable(Database db) throws SQLException {
        Statement st = db.getConnection().createStatement();
        String createTable = null;
        createTable = "CREATE TABLE " + QuickShop.instance
                .getDbPrefix() + "shops (owner  VARCHAR(255) NOT NULL, price  double(32, 2) NOT NULL, itemConfig TEXT CHARSET utf8 NOT NULL, x  INTEGER(32) NOT NULL, y  INTEGER(32) NOT NULL, z  INTEGER(32) NOT NULL, world VARCHAR(32) NOT NULL, unlimited  boolean, type  boolean, PRIMARY KEY (x, y, z, world) );";
        st.execute(createTable);
    }

    /**
     * Creates the database table 'messages'
     *
     * @throws SQLException If the connection is invalid
     */
    public boolean createMessagesTable(Database db) throws SQLException {
        Statement st = db.getConnection().createStatement();
        String createTable = null;
        createTable = "CREATE TABLE " + QuickShop.instance.getDbPrefix()
                + "messages (owner  VARCHAR(255) NOT NULL, message  TEXT(25) NOT NULL, time  BIGINT(32) NOT NULL );";
        return st.execute(createTable);
    }

    public ResultSet selectAllShops(Database db) throws SQLException {
        Statement st = db.getConnection().createStatement();
        String selectAllShops = "SELECT * FROM " + QuickShop.instance.getDbPrefix() + "shops";
        return st.executeQuery(selectAllShops);
    }

    public ResultSet selectAllMessages(Database db) throws SQLException {
        Statement st = db.getConnection().createStatement();
        String selectAllShops = "SELECT * FROM " + QuickShop.instance.getDbPrefix() + "messages";
        return st.executeQuery(selectAllShops);
    }

    public boolean removeShop(Database db, int x, int y, int z, String worldName) throws SQLException {
//		db.getConnection().createStatement()
//				.executeUpdate("DELETE FROM " + QuickShop.instance.getDbPrefix() + "shops WHERE x = " + x + " AND y = " + y
//						+ " AND z = " + z + " AND world = \"" + worldName + "\""
//						+ (db.getCore() instanceof MySQLCore ? " LIMIT 1" : ""));
        String sqlString = "DELETE FROM " + QuickShop.instance
                .getDbPrefix() + "shops WHERE x = ? AND y = ? AND z = ? AND world = ?" + (db.getCore() instanceof MySQLCore ?
                " LIMIT 1" :
                "");

        PreparedStatement ps = db.getConnection().prepareStatement(sqlString);
        ps.setInt(1, x);
        ps.setInt(2, y);
        ps.setInt(3, z);
        ps.setString(4, worldName);
        return ps.execute();
    }

    public void updateOwner2UUID(Database db, String ownerUUID, int x, int y, int z, String worldName)
            throws SQLException {
        //QuickShop.instance.getDB().getConnection().createStatement()
        //         .executeUpdate("UPDATE " + QuickShop.instance.getDbPrefix() + "shops SET owner = \"" + ownerUUID.toString()
        //                 + "\" WHERE x = " + x + " AND y = " + y + " AND z = " + z
        //                 + " AND world = \"" + worldName + "\" LIMIT 1");
        String sqlString = "UPDATE " + QuickShop.instance
                .getDbPrefix() + "shops SET owner = ? WHERE x = ? AND y = ? AND z = ? AND world = ?" + (db
                .getCore() instanceof MySQLCore ? " LIMIT 1" : "");
        PreparedStatement ps = db.getConnection().prepareStatement(sqlString);
        ps.setString(1, ownerUUID);
        ps.setInt(2, x);
        ps.setInt(3, y);
        ps.setInt(4, z);
        ps.setString(5, worldName);
        plugin.getDatabaseManager().add(ps);
    }

    public void updateShop(Database db, String owner, ItemStack item, int unlimited, int shopType,
                           double price, int x, int y, int z, String world) {
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
            plugin.getDatabaseManager().add(ps);
            //db.execute(q, owner, Util.serialize(item), unlimited, shopType, price, x, y, z, world);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

    }

    public void createShop(Database db, String owner, double price, ItemStack item, int unlimited, int shopType, String world, int x, int y, int z) {
        try {
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
            plugin.getDatabaseManager().add(ps);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void sendMessage(Database db, UUID player, String message, long time) {
        try {
            String sqlString = "INSERT INTO " + QuickShop.instance
                    .getDbPrefix() + "messages (owner, message, time) VALUES (?, ?, ?)";
            //QuickShop.instance.getDB().execute(q, player.toString(), message, System.currentTimeMillis());
            PreparedStatement ps = db.getConnection().prepareStatement(sqlString);
            ps.setString(1, player.toString());
            ps.setString(2, message);
            ps.setLong(3, time);
            plugin.getDatabaseManager().add(ps);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void cleanMessage(Database db, long weekAgo) {
        try {
            //QuickShop.instance.getDB().execute("DELETE FROM " + QuickShop.instance
            //        .getDbPrefix() + "messages WHERE time < ?", weekAgo);
            String sqlString = "DELETE FROM " + QuickShop.instance
                    .getDbPrefix() + "messages WHERE time < ?";
            PreparedStatement ps = db.getConnection().prepareStatement(sqlString);
            ps.setLong(1, weekAgo);
            plugin.getDatabaseManager().add(ps);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public void cleanMessageForPlayer(Database db, UUID player) {
        try {
            String sqlString = "DELETE FROM " + QuickShop.instance.getDbPrefix() + "messages WHERE owner = ?";
            PreparedStatement ps = db.getConnection().prepareStatement(sqlString);
            ps.setString(1, player.toString());
            plugin.getDatabaseManager().add(ps);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}