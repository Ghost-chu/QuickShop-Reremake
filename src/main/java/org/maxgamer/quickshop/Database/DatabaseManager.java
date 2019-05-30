package org.maxgamer.quickshop.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.scheduler.BukkitRunnable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

public class DatabaseManager {
    private QuickShop plugin;
    private Database database;
    private boolean useQueue;
    Queue<PreparedStatement> sqlQueue = new LinkedBlockingQueue<>();

    public DatabaseManager(QuickShop plugin, Database db) {
        this.plugin = plugin;
        this.database = db;
        this.useQueue = plugin.getConfig().getBoolean("database.queue");
        if (!useQueue)
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                while (true) {
                    PreparedStatement statement = sqlQueue.poll();
                    if (statement == null)
                        break;
                    try {
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
        }.runTaskTimer(plugin, 1, 200);
    }

    public void add(PreparedStatement ps) {
        if (useQueue) {
            Util.debugLog("Run task with queue");
            sqlQueue.offer(ps);
        } else {
            Util.debugLog("Run task without queue");
            try {
                ps.execute();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }
}
