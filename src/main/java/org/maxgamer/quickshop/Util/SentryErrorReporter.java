/*
 * This file is a part of project QuickShop, the name is SentryErrorReporter.java
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

package org.maxgamer.quickshop.Util;

import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import io.sentry.context.Context;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.UserBuilder;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Paste.Paste;
import org.maxgamer.quickshop.Watcher.UpdateWatcher;

/**
 * Auto report errors to qs's sentry.
 */
public class SentryErrorReporter {
  private final String dsn =
      "https://1d14223850ee44b284b11734461ebbc5@sentry.io/1473041?"
          + "stacktrace.app.packages=org.maxgamer.quickshop";

  private final ArrayList<String> reported = new ArrayList<>();

  private final Context context;

  private boolean disable;

  @Getter
  private boolean enabled;

  private final List<Class<?>> ignoredException = new ArrayList<>();

  private final QuickShop plugin;

  /* Pre-init it if it called before the we create it... */
  private final SentryClient sentryClient;

  private boolean tempDisable;

  private String lastPaste;

  private final IncompatibleChecker checker = new IncompatibleChecker();

  public SentryErrorReporter(@NotNull QuickShop plugin) {
    this.plugin = plugin;
    // sentryClient = Sentry.init(dsn);
    Util.debugLog("Loading SentryErrorReporter");
    sentryClient = SentryClientFactory.sentryClient(dsn);
    context = sentryClient.getContext();
    Util.debugLog("Setting basic report data...");
    // context.addTag("plugin_version", QuickShop.getVersion());
    context.addTag("system_os", System.getProperty("os.name"));
    context.addTag("system_arch", System.getProperty("os.arch"));
    context.addTag("system_version", System.getProperty("os.version"));
    context.addTag("system_cores", String.valueOf(Runtime.getRuntime().availableProcessors()));
    context.addTag("server_build", Bukkit.getServer().getVersion());
    context.addTag("server_java", String.valueOf(System.getProperty("java.version")));
    context.addTag(
        "server_players", Bukkit.getOnlinePlayers().size() + "/" + Bukkit.getMaxPlayers());
    context.addTag("server_onlinemode", String.valueOf(Bukkit.getOnlineMode()));
    context.addTag("server_bukkitversion", Bukkit.getVersion());
    context.addTag("server_plugins", getPluginInfo());
    context.setUser(
        new UserBuilder()
            .setId(plugin.getServerUniqueID().toString())
            .setUsername(plugin.getServerUniqueID().toString())
            .build());
    sentryClient.setServerName(
        Bukkit.getServer().getName() + " @ " + Bukkit.getServer().getVersion());
    sentryClient.setRelease(QuickShop.getVersion());
    sentryClient.setEnvironment(Util.isDevEdition() ? "development" : "production");
    plugin
        .getLogger()
        .setFilter(
            new QuickShopExceptionFilter()); // Redirect log request passthrough our error catcher.
    Bukkit.getLogger().setFilter(new GlobalExceptionFilter());
    Bukkit.getServer().getLogger().setFilter(new GlobalExceptionFilter());
    Logger.getGlobal().setFilter(new GlobalExceptionFilter());
    /* Ignore we won't report errors */
    ignoredException.add(IOException.class);
    ignoredException.add(OutOfMemoryError.class);
    ignoredException.add(ProtocolException.class);
    ignoredException.add(InvalidPluginException.class);
    ignoredException.add(UnsupportedClassVersionError.class);
    ignoredException.add(LinkageError.class);

    Util.debugLog("Sentry error reporter success loaded.");
    enabled = true;
    if (!plugin.getConfig().getBoolean("auto-report-errors")) {
      Util.debugLog("Sentry error report was disabled, unloading...");
      unit();
      return;
    }
    new BukkitRunnable() {
      @Override
      public void run() {
        Paste paste = new Paste(plugin);
        lastPaste = paste.paste(paste.genNewPaste(), 1);
        if (lastPaste != null) {
          plugin.log("Plugin booted up, the server paste was created for debugging, reporting errors and data-recovery: " + lastPaste);
        }
      }
    }.runTaskAsynchronously(plugin);
  }

  /**
   * Dupe report check
   *
   * @param throwable Throws
   * @return dupecated
   */
  public boolean canReport(@NotNull Throwable throwable) {
    if (!enabled) {
      return false;
    }
    if (UpdateWatcher.hasNewUpdate) { // We only receive latest reports.
      return false;
    }
    if (checker
        .isIncompatible(
            Util
                .getNMSVersion())) { // Ignore errors if user install quickshop on unsupported
                                     // version.
      return false;
    }
    if(!checkWasCauseByQS(throwable)){
      return false;
    }
    StackTraceElement stackTraceElement;
    if (throwable.getStackTrace().length < 3) {
      stackTraceElement = throwable.getStackTrace()[1];
    } else {
      stackTraceElement = throwable.getStackTrace()[2];
    }
    String text =
        stackTraceElement.getClassName()
            + "#"
            + stackTraceElement.getMethodName()
            + "#"
            + stackTraceElement.getLineNumber();
    if (!reported.contains(text)) {
      reported.add(text);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Check a throw is cause by QS
   *
   * @param throwable Throws
   * @return Cause or not
   */
  public boolean checkWasCauseByQS(@Nullable Throwable throwable) {
    if (throwable == null) {
      return false;
    }
    if (throwable.getMessage() == null) {
      return false;
    }
    if (throwable.getMessage().contains("Could not pass event")) {
      return throwable.getMessage().contains("QuickShop");
    }
    while (throwable.getCause() != null) {
      throwable = throwable.getCause();
    }
    long element =
        Arrays.stream(throwable.getStackTrace())
            .limit(5)
            .filter(
                stackTraceElement ->
                    stackTraceElement.getClassName().contains("org.maxgamer.quickshop"))
            .count();
    if(element > 0){
      return true;
    }else if(throwable.getCause() != null){
      return checkWasCauseByQS(throwable.getCause());
    }
    return false;
  }

  /** Set ignore throw. It will unlocked after accept a throw */
  public void ignoreThrow() {
    tempDisable = true;
  }

  /** Set ignore throws. It will unlocked after called method resetIgnores. */
  public void ignoreThrows() {
    disable = true;
  }

  /** Reset ignore throw(s). */
  public void resetIgnores() {
    tempDisable = false;
    disable = false;
  }

  /**
   * Send a error to Sentry
   *
   * @param throwable Throws
   * @param context BreadCrumb
   * @return Event Uniqud ID
   */
  public @Nullable UUID sendError(@NotNull Throwable throwable, @NotNull String... context) {
    try {
      if (QuickShop.instance.getBootError() != null) {
        return null; // Don't report any errors if boot failed.
      }
      if (tempDisable) {
        this.tempDisable = false;
        return null;
      }
      if (disable) {
        return null;
      }
      if (!enabled) {
        return null;
      }

      if (!checkWasCauseByQS(throwable)) {
        return null;
      }

      if (!canReport(throwable)) {
        return null;
      }
      if (ignoredException.contains(throwable.getClass())) {
        return null;
      }
      for (String record : context) {
        this.context.recordBreadcrumb(new BreadcrumbBuilder().setMessage(record).build());
      }
      String pasteURL = "Failed to paste.";
      try {
        Paste paste = new Paste(plugin);
        pasteURL = paste.paste(paste.genNewPaste());
        if (pasteURL != null && !pasteURL.isEmpty()) {
          lastPaste = pasteURL;
        }
      } catch (Throwable ex) {
        // Ignore
        pasteURL = this.lastPaste;
      }
      this.context.addTag("paste", pasteURL);
      this.sentryClient.sendException(throwable);
      this.context.clearBreadcrumbs();
      plugin
          .getLogger()
          .warning(
              "A exception was thrown, QuickShop already caught this exception and reported it, switch to debug mode to see the full errors.");
      plugin.getLogger().warning("====QuickShop Error Report BEGIN===");
      plugin.getLogger().warning("Description: " + throwable.getMessage());
      plugin.getLogger().warning("Event    ID: " + this.context.getLastEventId());
      plugin.getLogger().warning("Server   ID: " + plugin.getServerUniqueID());
      plugin.getLogger().warning("====QuickShop Error Report E N D===");
      if (Util.isDevMode()) {
        throwable.printStackTrace();
      }
      return this.context.getLastEventId();
    } catch (Throwable th) {
      th.printStackTrace();
      return null;
    }
  }

  /** Unload Sentry error reporter */
  private void unit() {
    enabled = false;
  }

  private String getPluginInfo() {
    StringBuilder buffer = new StringBuilder();
    for (Plugin bplugin : Bukkit.getPluginManager().getPlugins()) {
      buffer
          .append("\t")
          .append(bplugin.getName())
          .append("@")
          .append(bplugin.isEnabled() ? "Enabled" : "Disabled")
          .append("\n");
    }
    return buffer.toString();
  }

  class GlobalExceptionFilter implements Filter {

    /**
     * Check if a given log record should be published.
     *
     * @param record a LogRecord
     * @return true if the log record should be published.
     */
    @Override
    public boolean isLoggable(@NotNull LogRecord record) {
      if (!enabled) {
        return true;
      }
      Level level = record.getLevel();
      if (level != Level.WARNING && level != Level.SEVERE) {
        return true;
      }
      if (Util.isDevMode()) {
        sendError(record.getThrown(), record.getMessage());
        return true;
      } else {
        return sendError(record.getThrown(), record.getMessage()) == null;
      }
    }
  }

  class QuickShopExceptionFilter implements Filter {

    /**
     * Check if a given log record should be published.
     *
     * @param record a LogRecord
     * @return true if the log record should be published.
     */
    @Override
    public boolean isLoggable(@NotNull LogRecord record) {
      if (!enabled) {
        return true;
      }
      Level level = record.getLevel();
      if (level != Level.WARNING && level != Level.SEVERE) {
        return true;
      }
      if (record.getThrown() == null) {
        return true;
      }
      if (Util.isDevMode()) {
        sendError(record.getThrown(), record.getMessage());
        return true;
      } else {
        return sendError(record.getThrown(), record.getMessage()) == null;
      }
    }
  }
}
