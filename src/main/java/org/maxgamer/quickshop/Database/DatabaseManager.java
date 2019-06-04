package org.maxgamer.quickshop.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

public class DatabaseManager {
    private QuickShop plugin;
    private Database database;
    private boolean useQueue;
    Queue<PreparedStatement> sqlQueue = new LinkedBlockingQueue<>();
    BukkitTask task;
    public DatabaseManager(QuickShop plugin, Database db) {
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

    public void uninit() {
        if ((task != null) && !task.isCancelled())
            task.cancel();
        plugin.getLogger().info("Please waiting for flushing data to database...");
        runTask();
    }

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

    public void add(PreparedStatement ps) {
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
