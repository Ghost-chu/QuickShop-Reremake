package org.maxgamer.quickshop.Watcher;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class LogWatcher extends BukkitRunnable {
    private Queue<String> logs = new LinkedList<>();
    private FileWriter logFileWriter = null;

    public LogWatcher(QuickShop plugin, File log) {
        try {
            if (!log.exists()) {
                log.createNewFile();
            }
            logFileWriter = new FileWriter(log);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Log file was not found!");
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Could not create the log file!");
        }
    }

    public void add(@NotNull String s) {
        logs.add(s);
    }

    @SneakyThrows
    public void close() {
        if (logFileWriter != null) {
            logFileWriter.flush();
            logFileWriter.close();
        }
    }

    public void log(@NonNull String log) {
        Date date = Calendar.getInstance().getTime();
        Timestamp time = new Timestamp(date.getTime());
        this.add("[" + time.toString() + "] " + log);
    }

    @Override
    public void run() {
        for (String log : logs) {
            if (logFileWriter == null) {
                continue;
            }
            try {
                logFileWriter.write(log + System.getProperty("line.separator"));
            } catch (IOException ioe) {
                Util.debugLog("Failed to write log to disk: " + ioe.getMessage() + ", the log was dropped.");
            }
        }
        logs.clear();
        if (logFileWriter != null) {
            try {
                logFileWriter.flush();
            } catch (IOException ioe) {
                Util.debugLog("Failed to flush log to disk: " + ioe.getMessage());
            }
        }
    }
}