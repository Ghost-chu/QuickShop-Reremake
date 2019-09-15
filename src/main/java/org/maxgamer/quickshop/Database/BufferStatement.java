package org.maxgamer.quickshop.Database;

import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Util.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;


public class BufferStatement {
    private String query;
    private Object[] values;

    /**
     * Represents a PreparedStatement in a state before preparing it (E.g. No
     * file I/O Required)
     *
     * @param query  The query to execute. E.g. INSERT INTO accounts (user, passwd)
     *               VALUES (?, ?)
     * @param values The values to replace ? with in
     *               query. These are in order.
     */
    public BufferStatement(@NotNull String query, @NotNull Object... values) {
        this.query = query;
        Util.debugLog(query);
        this.values = values;
    }

    /**
     * Returns a prepared statement using the given connection. Will try to
     * return an empty statement if something went wrong. If that fails, returns
     * null.
     * <p>
     * This method escapes everything automatically.
     *
     * @param con The connection to prepare this on using
     *            con.prepareStatement(..)
     * @return The prepared statement, ready for execution.
     * @throws SQLException Throw exception when failed to execute something in SQL
     */
    PreparedStatement prepareStatement(@NotNull Connection con) throws SQLException {
        PreparedStatement ps;
        Util.debugLog(query);
        ps = con.prepareStatement(query);
        for (int i = 1; i <= values.length; i++) {
            ps.setObject(i, values[i - 1]);
        }
        return ps;
    }

    /**
     * @return A string representation of this statement. Returns
     * "Query: " + query + ", values: " + Arrays.toString(values).
     */
    @Override
    public String toString() {
        return "Query: " + query + ", values: " + Arrays.toString(values);
    }
}