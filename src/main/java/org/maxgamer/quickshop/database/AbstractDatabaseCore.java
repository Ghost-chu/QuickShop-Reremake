/*
 * This file is a part of project QuickShop, the name is AbstractDatabaseCore.java
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

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

/**
 * DatabaseCore abstract
 *
 * @author sandtechnology
 */
public abstract class AbstractDatabaseCore {
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Condition conditionLock = lock.newCondition();

    void waitForConnection() {
        lock.lock();
        try {
            conditionLock.await();
        } catch (InterruptedException e) {
            getPlugin().getLogger().log(Level.SEVERE, "Exception when waiting new database connection", e);
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    void signalForNewConnection() {
        lock.lock();
        try {
            conditionLock.signal();
        } finally {
            lock.unlock();
        }

    }

    /**
     * Close all not in-use connections created by DatabaseCore.
     */
    abstract void close();

    /**
     * Gets the database connection for executing queries on.
     *
     * @return The database connection, PLEASE MAKE SURE USING DatabaseConnection#release to CLOSE THE CONNECTION
     */
    @NotNull
    synchronized DatabaseConnection getConnection() {
        DatabaseConnection databaseConnection = getConnection0();
        databaseConnection.markUsing();
        return databaseConnection;
    }

    abstract protected DatabaseConnection getConnection0();

    /**
     * Getting DatabaseCore impl name
     *
     * @return Impl name
     */
    abstract public @NotNull String getName();

    /**
     * Getting DatabaseCore owned by
     *
     * @return Owned by
     */
    abstract public @NotNull Plugin getPlugin();

}
