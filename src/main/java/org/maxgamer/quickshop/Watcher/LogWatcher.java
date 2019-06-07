package org.maxgamer.quickshop.Watcher;

import java.io.*;
import java.util.Iterator;
import java.util.Queue;

import com.google.common.collect.Lists;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.QuickShop;

public class LogWatcher implements Runnable {
    private PrintStream ps;
    private Queue<String> logs = Lists.newLinkedList();
    public BukkitTask task;

    public LogWatcher(QuickShop plugin, File log) {
        try {
            if (!log.exists()) {
                log.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(log, true);
            this.ps = new PrintStream(fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Log file not found!");
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Could not create log file!");
        }
    }

    @Override
    public void run() {
        new BukkitRunnable() {
            public void run() {
                Iterator<String> iterator = logs.iterator();
                while (iterator.hasNext()) {
                    ps.print(iterator.next());
                }
                logs.clear();

            }
        }.runTask(QuickShop.instance);
    }

    public void add(@NotNull String s) {
        new BukkitRunnable() {
            public void run() {
                logs.add(s);
            }
        }.runTask(QuickShop.instance);

    }

    public void close() {
        if (ps != null)
            this.ps.close();
        ps = null;
    }
}