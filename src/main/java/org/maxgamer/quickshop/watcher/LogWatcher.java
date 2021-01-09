/*
 * This file is a part of project QuickShop, the name is LogWatcher.java
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

package org.maxgamer.quickshop.watcher;

import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

public class LogWatcher extends BukkitRunnable implements AutoCloseable {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter logFileFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
    private final Queue<String> logs = new ConcurrentLinkedQueue<>();
    private FileWriter logFileWriter = null;

    private PrintWriter pw;

    public LogWatcher(QuickShop plugin, File log) {
        try {
            if (!log.exists()) {
                //noinspection ResultOfMethodCallIgnored
                log.createNewFile();
            } else {
                if ((log.length() / 1024f / 1024f) > plugin.getConfig().getDouble("logging.file-size")) {
                    Path logPath = plugin.getDataFolder().toPath().resolve("logs");
                    Files.createDirectories(logPath);
                    //Find a available name
                    Path targetPath;
                    int i = 1;
                    do {
                        targetPath = logPath.resolve(ZonedDateTime.now().format(logFileFormatter) + "-" + i + ".log.gz");
                        i++;
                    } while (Files.exists(targetPath));
                    Files.createFile(targetPath);
                    GzipParameters gzipParameters = new GzipParameters();
                    gzipParameters.setFilename(log.getName());
                    try (GzipCompressorOutputStream archiveOutputStream = new GzipCompressorOutputStream(new BufferedOutputStream(new FileOutputStream(targetPath.toFile())), gzipParameters)) {
                        Files.copy(log.toPath(), archiveOutputStream);
                        archiveOutputStream.finish();
                        //noinspection ResultOfMethodCallIgnored
                        log.delete();
                        //noinspection ResultOfMethodCallIgnored
                        log.createNewFile();
                    }
                }
            }
            logFileWriter = new FileWriter(log, true);
            pw = new PrintWriter(logFileWriter);
        } catch (FileNotFoundException e) {
            plugin.getLogger().log(Level.SEVERE, "Log file was not found!", e);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create the log file!", e);
        }
    }

    @SneakyThrows
    public void close() {
        if (logFileWriter != null) {
            logFileWriter.flush();
            logFileWriter.close();
        }
    }

    public void log(@NonNull String log) {
        this.add("[" + dateTimeFormatter.format(Instant.now()) + "] " + log);
    }

    public void add(@NotNull String s) {
        logs.add(s);
    }

    @Override
    public void run() {
        for (String log : logs) {
            if (logFileWriter == null) {
                continue;
            }
            if (pw == null) {
                continue;
            }
            pw.println(log);
        }
        logs.clear();
        if (logFileWriter != null) {
            try {
                if (pw != null) {
                    pw.flush();
                }
                logFileWriter.flush();
            } catch (IOException ioe) {
                Util.debugLog("Failed to flush log to disk: " + ioe.getMessage());
            }
        }
    }

}
