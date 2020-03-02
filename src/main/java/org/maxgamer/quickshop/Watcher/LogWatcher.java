/*
 * This file is a part of project QuickShop, the name is LogWatcher.java
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

package org.maxgamer.quickshop.Watcher;

import java.io.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

public class LogWatcher extends BukkitRunnable {
  private final Queue<String> logs = new ConcurrentLinkedQueue<>();

  private FileWriter logFileWriter = null;

  private PrintWriter pw;

  public LogWatcher(QuickShop plugin, File log) {
    try {
      if (!log.exists()) {
        log.createNewFile();
      }
      logFileWriter = new FileWriter(log, true);
      pw = new PrintWriter(logFileWriter);
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
    this.add("[" + time + "] " + log);
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
