package org.maxgamer.quickshop.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

public class Database {
    private DatabaseCore core;
    // Fix null pointer...
    private QuickShop plugin = QuickShop.instance;

    /**
     * Creates a new database and validates its connection.
     * <p>
     * If the connection is invalid, this will throw a ConnectionException.
     *
     * @param core The core for the database, either MySQL or SQLite.
     * @throws ConnectionException If the connection was invalid
     */
    public Database(@NotNull DatabaseCore core) throws ConnectionException {
        try {
            try {
                if (!core.getConnection().isValid(10)) {
                    throw new ConnectionException("The database does not appear to be valid!");
                }
            } catch (AbstractMethodError e) {
                // You don't need to validate this core.
            }
        } catch (SQLException e) {
            throw new ConnectionException(e.getMessage());
        }
        this.core = core;
    }

    /**
     * Closes the database
     */
    public void close() {
        this.core.close();
    }

    /**
     * Copies the contents of this database into the given database. Does not
     * delete the contents of this database, or change any settings. This may
     * take a long time, and will print out progress reports to System.out
     * <p>
     * This method does not create the tables in the new database. You need to
     * do that yourself.
     *
     * @param db The database to copy data to
     * @throws SQLException if an error occurs.
     */
    @Deprecated /* Buggy, owner pls use Database Tools to migrate */
    public void copyTo(@NotNull Database db) throws SQLException {
        ResultSet rs = getConnection().getMetaData().getTables(null, null, "%", null);
        List<String> tables = new LinkedList<String>();
        while (rs.next()) {
            tables.add(rs.getString("TABLE_NAME"));
        }
        rs.close();
        core.flush();
        // For each table
        String prefix = plugin.getConfig().getString("database.prefix");
        for (String table : tables) {
            if (table.contains("schedule"))
                return; // go way!
            String finalTable;
            if (table.startsWith(prefix)) {
                finalTable = table;
            } else {
                finalTable = prefix + table;
                plugin.getLogger().info("CovertHelper: Fixed table name from SQLite " + table + " to MySQL " + finalTable);
            }

            if (table.toLowerCase().startsWith("sqlite_autoindex_"))
                continue;
            plugin.getLogger().log(Level.WARNING, "Copying " + table + " to " + finalTable);
            // Wipe the old records
            db.getConnection().prepareStatement("DELETE FROM " + finalTable).execute();
            // Fetch all the data from the existing database
            rs = getConnection().prepareStatement("SELECT * FROM " + table).executeQuery();
            int n = 0;
            // Build the query
            StringBuilder query = new StringBuilder("INSERT INTO " + finalTable + " VALUES (");
            // Append another placeholder for the value
            query.append("?");
            for (int i = 2; i <= rs.getMetaData().getColumnCount(); i++) {
                // Add the rest of the placeholders and values. This is so we
                // have (?, ?, ?) and not (?, ?, ?, ).
                query.append(", ?");
            }
            // End the query
            query.append(")");
            PreparedStatement ps = db.getConnection().prepareStatement(query.toString());
            while (rs.next()) {
                n++;
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    ps.setObject(i, rs.getObject(i));
                }
                ps.addBatch();
                if (n % 100 == 0) {
                    ps.executeBatch();
                    plugin.getLogger().log(Level.WARNING, n + " records copied...");
                }
            }
            ps.executeBatch();
            // Close the resultset of that table
            rs.close();
        }
        // Success!
        db.getConnection().close();
        this.getConnection().close();
    }

    /**
     * Executes the given statement either immediately, or soon.
     *
     * @param query The query
     * @param objs  The string values for each ? in the given query.
     */
    public void execute(@NotNull String query, @NotNull Object... objs) {
        Util.debugLog(query);
        BufferStatement bs = new BufferStatement(query, objs);
        core.queue(bs);
    }

    /**
     * Returns true if the given table has the given column
     *
     * @param table  The table
     * @param column The column
     * @return True if the given table has the given column
     * @throws SQLException If the database isn't connected
     */
    public boolean hasColumn(@NotNull String table, @NotNull String column) throws SQLException {
        if (!hasTable(table))
            return false;
        String query = "SELECT * FROM " + table + " LIMIT 0,1";
        try {
            PreparedStatement ps = this.getConnection().prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString(column) != null)
                    return true;
            }
        } catch (SQLException e) {
            return false;
        }
        return false; // Uh, wtf.
    }

    /**
     * Returns true if the table exists
     *
     * @param table The table to check for
     * @return True if the table is found
     * @throws SQLException Throw exception when failed execute somethins on SQL
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean hasTable(@NotNull String table) throws SQLException {
        ResultSet rs = getConnection().getMetaData().getTables(null, null, "%", null);
        while (rs.next()) {
            if (table.equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                rs.close();
                return true;
            }
        }
        rs.close();
        return false;
    }

    /**
     * Fetches the connection to this database for querying. Try to avoid doing
     * this in the main thread.
     *
     * @return Fetches the connection to this database for querying.
     */
    public Connection getConnection() {
        return core.getConnection();
    }

    /**
     * Returns the database core object, that this database runs on.
     *
     * @return the database core object, that this database runs on.
     */
    public DatabaseCore getCore() {
        return core;
    }

    /**
     * Represents a connection error, generally when the server can't connect to
     * MySQL or something.
     */
    public static class ConnectionException extends Exception {
        private static final long serialVersionUID = 8348749992936357317L;

        private ConnectionException(String msg) {
            super(msg);
        }
    }
}