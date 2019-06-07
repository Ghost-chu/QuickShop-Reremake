package org.maxgamer.quickshop.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

/**
 * Queued database manager.
 * Use queue to solve run SQL make server lagg issue.
 */
public class DatabaseManager {
    private QuickShop plugin;
    private Database database;
    private boolean useQueue;
    private Queue<PreparedStatement> sqlQueue = new LinkedBlockingQueue<>();
    private BukkitTask task;

    /**
     * Queued database manager.
     * Use queue to solve run SQL make server lagg issue.
     *
     * @param plugin plugin main class
     * @param db database
     */
    public DatabaseManager(@NotNull QuickShop plugin, @NotNull Database db) {
        this.plugin = plugin;
        this.database = db;
        this.useQueue = plugin.getConfig().getBoolean("database.queue");
        if (!useQueue)
            return;
        task = new BukkitRunnable() {
            @Override
            public void run() {
                plugin.getDatabaseManager().runTask();
            }
        }.runTaskTimer(plugin, 1, 200);
    }

    /**
     * Unload the DatabaseManager, run at onDisable()
     */
    public void uninit() {
        if ((task != null) && !task.isCancelled())
            task.cancel();
        plugin.getLogger().info("Please waiting for flushing data to database...");
        runTask();
    }

    /**
     * Internal method, runTasks in queue.
     */
    private void runTask() {
        while (true) {
            PreparedStatement statement = sqlQueue.poll();
            if (statement == null)
                break;
            try {
                Util.debugLog("Executing the SQL task...");
                statement.execute();

            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }

            //Close statement anyway.
            try {
                statement.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }

        }
    }

    /**
     * Add preparedStatement to queue waiting flush to database,
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
}
