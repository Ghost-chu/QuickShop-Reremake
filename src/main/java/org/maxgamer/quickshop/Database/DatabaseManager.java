/*
 * This file is a part of project QuickShop, the name is DatabaseManager.java
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

import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Timer;
import org.maxgamer.quickshop.Util.Util;
import org.maxgamer.quickshop.Util.WarningSender;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Queued database manager.
 * Use queue to solve run SQL make server lagg issue.
 */
public class DatabaseManager {

    private final Queue<PreparedStatement> sqlQueue = new LinkedBlockingQueue<>();

    @NotNull
    private final Database database;

    @NotNull
    private final QuickShop plugin;

    @Nullable
    private BukkitTask task;

    private boolean useQueue;

    @NotNull
    private final WarningSender warningSender;

    /**
     * Queued database manager.
     * Use queue to solve run SQL make server lagg issue.
     *
     * @param plugin plugin main class
     * @param db     database
     */
    public DatabaseManager(@NotNull QuickShop plugin, @NotNull Database db) {
        this.plugin = plugin;
        this.warningSender = new WarningSender(plugin, 600000);
        this.database = db;
        this.useQueue = plugin.getConfig().getBoolean("database.queue");
        if (!useQueue) {
            return;
        }
        try {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    plugin.getDatabaseManager().runTask();
                }
            }.runTaskTimerAsynchronously(plugin, 1, 200);
        } catch (IllegalPluginAccessException e) {
            Util.debugLog("Plugin is disabled but trying create database task, move to Main Thread...");
            plugin.getDatabaseManager().runTask();
        }
    }

    /**
     * Add preparedStatement to queue waiting flush to database,
     *
     * @param ps The ps you want add in queue.
     */
    public void add(@NotNull PreparedStatement ps) {
        if (useQueue) {
            sqlQueue.offer(ps);
        } else {
            try {
                ps.execute();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }

    /**
     * Internal method, runTasks in queue.
     */
    private void runTask() {
        while (true) {
            Timer timer = new Timer(true);
            try {
                if (!this.database.getConnection().isValid(3000)) {
                    this.plugin.getLogger()
                            .warning("Database connection may lost, we are trying reconnecting, if this message appear too many times, you should check your database file(sqlite) and internet connection(mysql).");
                    break; //Waiting next crycle and hope it success reconnected.
                }
            } catch (SQLException sqle) {
                plugin.getSentryErrorReporter().ignoreThrow();
                sqle.printStackTrace();
            }
            PreparedStatement statement = sqlQueue.poll();
            if (statement == null) {
                break;
            }
            try {
                Util.debugLog("Executing the SQL task: " + statement);
                statement.execute();
            } catch (SQLException sqle) {
                plugin.getSentryErrorReporter().ignoreThrow();
                sqle.printStackTrace();
            }
            //Close statement anyway.
            try {
                statement.close();
            } catch (SQLException sqle) {
                plugin.getSentryErrorReporter().ignoreThrow();
                sqle.printStackTrace();
            }
            long tookTime = timer.endTimer();
            if (tookTime > 5000) {
                warningSender
                        .sendWarn("Database performance warning: It took too long time (" + tookTime + "ms) to execute the task, it may cause the network connection with MySQL server or just MySQL server too slow, change to a better MySQL server or switch to a local SQLite database!");
            }
        }
        try {
            this.database.getConnection().commit();
        }catch (SQLException e){
            try {
                this.database.getConnection().rollback();
            }catch (SQLException ignored){
            }
        }
    }

    /**
     * Unload the DatabaseManager, run at onDisable()
     */
    public void unInit() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        plugin.getLogger().info("Please wait for the data to flush its data...");
        runTask();
    }
}
