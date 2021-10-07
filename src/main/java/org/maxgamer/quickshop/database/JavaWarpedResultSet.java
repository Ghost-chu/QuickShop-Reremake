/*
 * This file is a part of project QuickShop, the name is WarpedResultSet.java
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

import lombok.Getter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JavaWarpedResultSet implements org.maxgamer.quickshop.api.database.WarpedResultSet,AutoCloseable {
    @Getter
    private final ResultSet resultSet;
    private final DatabaseConnection databaseConnection;
    private final Statement statement;

    public JavaWarpedResultSet(Statement statement, ResultSet resultSet, DatabaseConnection databaseConnection) {
        this.statement = statement;
        this.resultSet = resultSet;
        this.databaseConnection = databaseConnection;
    }

    @Override
    public void close() throws SQLException {
        resultSet.close();
        statement.close();
        databaseConnection.release();
    }
}
