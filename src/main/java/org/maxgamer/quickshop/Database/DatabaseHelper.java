package org.maxgamer.quickshop.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
	public static void setup(Database db) throws SQLException {
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
		Statement st = db.getConnection().createStatement();
		//String createTable = "CREATE TABLE shops (" + "owner  TEXT(32) NOT NULL, " + "price  double(32, 2) NOT NULL, " + "itemConfig TEXT CHARSET utf8 NOT NULL, " + "x  INTEGER(32) NOT NULL, " + "y  INTEGER(32) NOT NULL, " + "z  INTEGER(32) NOT NULL, " + "world VARCHAR(32) NOT NULL, " + "unlimited  boolean, " + "type  boolean, " + "PRIMARY KEY (x, y, z, world) " + ");";
		String createTable ="CREATE TABLE schedule (owner TEXT(32) NOT NULL, world VARCHAR(32) NOT NULL, x INT NOT NULL, y INT NOT NULL, z INT NOT NULL, timestamp INT NOT NULL, PRIMARY KEY (owner, world, x, y, z, timestamp) );";
		st.execute(createTable);
	}

	/**
	 * Verifies that all required columns exist.
	 */
	public static void checkColumns(Database db) {
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
		Statement st = db.getConnection().createStatement();
		String createTable = "CREATE TABLE shops (" + "owner  TEXT(32) NOT NULL, " + "price  double(32, 2) NOT NULL, " + "itemConfig TEXT CHARSET utf8 NOT NULL, " + "x  INTEGER(32) NOT NULL, " + "y  INTEGER(32) NOT NULL, " + "z  INTEGER(32) NOT NULL, " + "world VARCHAR(32) NOT NULL, " + "unlimited  boolean, " + "type  boolean, " + "PRIMARY KEY (x, y, z, world) " + ");";
		st.execute(createTable);
	}

	/**
	 * Creates the database table 'messages'
	 * 
	 * @throws SQLException
	 *             If the connection is invalid
	 */
	public static void createMessagesTable(Database db) throws SQLException {
		Statement st = db.getConnection().createStatement();
		String createTable = "CREATE TABLE messages (" + "owner  TEXT(32) NOT NULL, " + "message  TEXT(200) NOT NULL, " + "time  BIGINT(32) NOT NULL " + ");";
		st.execute(createTable);
	}
	
}