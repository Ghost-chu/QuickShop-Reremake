package org.maxgamer.quickshop.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

public class DatabaseHelper {
	public static void setup(Database db) throws SQLException {
		connectCheck();
		if (!db.hasTable("shops")) {
			createShopsTable(db);
		}
		if (!db.hasTable("messages")) {
			createMessagesTable(db);
		}
		if (!db.hasTable("schedule")) {
			createScheduleTable(db);
		}
		checkColumns(db);
	}
	
	/**
	 * Creates the database table 'schedule'.
	 * 
	 * @throws SQLException
	 *             If the connection is invalid.
	 */
	public static void createScheduleTable(Database db) throws SQLException {
		connectCheck();
		Statement st = db.getConnection().createStatement();
		//String createTable = "CREATE TABLE shops (" + "owner  TEXT(32) NOT NULL, " + "price  double(32, 2) NOT NULL, " + "itemConfig TEXT CHARSET utf8 NOT NULL, " + "x  INTEGER(32) NOT NULL, " + "y  INTEGER(32) NOT NULL, " + "z  INTEGER(32) NOT NULL, " + "world VARCHAR(32) NOT NULL, " + "unlimited  boolean, " + "type  boolean, " + "PRIMARY KEY (x, y, z, world) " + ");";
		String createTable ="CREATE TABLE schedule (owner TEXT(32) NOT NULL, world VARCHAR(32) NOT NULL, x INT NOT NULL, y INT NOT NULL, z INT NOT NULL, timestamp INT NOT NULL, PRIMARY KEY (owner, world, x, y, z, timestamp) );";
		st.execute(createTable);
	}

	/**
	 * Verifies that all required columns exist.
	 */
	public static void checkColumns(Database db) {
		connectCheck();
		PreparedStatement ps = null;
		try {
			// V3.4.2
			ps = db.getConnection().prepareStatement("ALTER TABLE shops MODIFY COLUMN price double(32,2) NOT NULL AFTER owner");
			ps.execute();
			ps.close();
		} catch (SQLException e) {
		}
		try {
			// V3.4.3
			ps = db.getConnection().prepareStatement("ALTER TABLE messages MODIFY COLUMN time BIGINT(32) NOT NULL AFTER message");
			ps.execute();
			ps.close();
		} catch (SQLException e) {
		}
	}

	/**
	 * Creates the database table 'shops'.
	 * 
	 * @throws SQLException
	 *             If the connection is invalid.
	 */
	public static void createShopsTable(Database db) throws SQLException {
		connectCheck();
		Statement st = db.getConnection().createStatement();
		String createTable = "CREATE TABLE shops (owner  VARCHAR(32) NOT NULL, price  double(32, 2) NOT NULL, itemConfig TEXT CHARSET utf8 NOT NULL, x  INTEGER(32) NOT NULL, y  INTEGER(32) NOT NULL, z  INTEGER(32) NOT NULL, world VARCHAR(32) NOT NULL, unlimited  boolean, type  boolean, PRIMARY KEY (x, y, z, world) );";
		st.execute(createTable);
	}

	/**
	 * Creates the database table 'messages'
	 * 
	 * @throws SQLException
	 *             If the connection is invalid
	 */
	public static void createMessagesTable(Database db) throws SQLException {
		connectCheck();
		Statement st = db.getConnection().createStatement();
		String createTable = "CREATE TABLE messages (owner  VARCHAR(32) NOT NULL, message  TEXT(25) NOT NULL, time  BIGINT(32) NOT NULL );";
		st.execute(createTable);
	}
	
	public static ResultSet selectAllShops(Database db) throws SQLException {
		connectCheck();
		PreparedStatement ps =  db.getConnection().prepareStatement("SELECT * FROM shops");
		return ps.executeQuery();
		
	}
	public static void removeShop(Database db, int x, int y, int z, String worldName) throws SQLException {
		connectCheck();
		db.getConnection().createStatement()
		.executeUpdate("DELETE FROM shops WHERE x = " + x + " AND y = " + y + " AND z = " + z
				+ " AND world = \"" + worldName + "\""
				+ (db.getCore() instanceof MySQLCore ? " LIMIT 1" : ""));
		db.getConnection().createStatement()
		.executeUpdate("DELETE FROM schedule WHERE x = " + x + " AND y = " + y + " AND z = " + z
				+ " AND world = \"" + worldName + "\""
				+ (db.getCore() instanceof MySQLCore ? " LIMIT 1" : ""));
	}
	public static void insertSchedule(String argUUID, String world, int x, int y, int z, long l) {
		connectCheck();
		String scheduleq = "INSERT INTO schedule (owner, world, x, y, z, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
		QuickShop.instance.getDB().execute(scheduleq , argUUID, world, x, y, z, System.currentTimeMillis());
	}

	public static void updateOwner2UUID(String ownerUUID, int x, int y, int z, String worldName) throws SQLException {
		connectCheck();
		QuickShop.instance.getDB().getConnection().createStatement()
		.executeUpdate("UPDATE shops SET owner = \"" + ownerUUID.toString()
		+ "\" WHERE x = " + x + " AND y = " + y + " AND z = " + z
		+ " AND world = \"" + worldName + "\" LIMIT 1");
		
	}

	public static void updateShop(Database db, String owner, ItemStack item, int unlimited, int shopType,
			double price, int x, int y, int z, String world) {
		connectCheck();
		String q = "UPDATE shops SET owner = ?, itemConfig = ?, unlimited = ?, type = ?, price = ? WHERE x = ? AND y = ? and z = ? and world = ?";
		db.execute(q, owner, Util.serialize(item), unlimited, shopType, price, x, y, z, world);
	}
	
	public static void createShop(String owner, double price, ItemStack item, int unlimited, int shopType, String world, int x, int y, int z) {
		connectCheck();
		String q = "INSERT INTO shops (owner, price, itemConfig, x, y, z, world, unlimited, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
		QuickShop.instance.getDB().execute(q, owner, price, Util.serialize(item), x, y, z, world, unlimited, shopType);
		// Reremake write in schedule data
		String scheduleq = "INSERT INTO schedule (owner, world, x, y, z, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
		QuickShop.instance.getDB().execute(scheduleq , owner, world,x,y,z, System.currentTimeMillis());
	}
	
	public static void connectCheck(){
		if(!QuickShop.instance.getConfig().getBoolean("database.reconnect")) {
			return;
		}
		try {
			if(QuickShop.instance.getDB().getConnection().isClosed())
			QuickShop.instance.getLogger().severe("Database connection lost, Reconnecting...");
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			if(QuickShop.instance.getDB().getConnection().isReadOnly()) {
			QuickShop.instance.getLogger().severe("Database is read-only, QSRR can't write in data!");
			return;
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		try {
			try {
				if (!QuickShop.instance.getDB().getConnection().isValid(10)) {
					boolean result = QuickShop.instance.setupDatabase(); // Reconnect
					if(result) {
						//Failed reconnect.
						QuickShop.instance.getLogger().severe("Failed to reconnect.");
					}else {
						QuickShop.instance.getLogger().severe("Reconnected.");
						connectCheck(); //Recall to check
					}
				}
			} catch (AbstractMethodError e) {
				// You don't need to validate this core.
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
}