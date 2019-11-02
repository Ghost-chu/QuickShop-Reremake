package org.maxgamer.quickshop.Watcher;

import lombok.NonNull;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;

import java.io.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class LogWatcher implements Runnable {
    private Queue<String> logs = new LinkedList<>();
    private PrintStream ps;

    public LogWatcher(QuickShop plugin, File log) {
        try {
            if (!log.exists()) {
                log.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(log, true);
            this.ps = new PrintStream(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Log file was not found!");
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Could not create the log file!");
        }
    }

    public void add(@NotNull String s) {
        new BukkitRunnable() {
            @Override
            public void run() {
                logs.add(s);
            }
        }.runTask(QuickShop.instance);

    }

    public void close() {
        if (ps != null) {
            this.ps.close();
        }
        ps = null;
    }

    public void log(@NonNull String log) {
        Date date = Calendar.getInstance().getTime();
        Timestamp time = new Timestamp(date.getTime());
        this.add("[" + time.toString() + "] " + log);
    }

    @Override
    public void run() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (String log : logs) {
                    if (ps == null) {
                        continue;
                    }
                    ps.print(log + "\n");
                }
                logs.clear();

            }
        }.runTaskAsynchronously(QuickShop.instance);
    }
}