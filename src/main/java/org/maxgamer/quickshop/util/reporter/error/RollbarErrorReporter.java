/*
 * This file is a part of project QuickShop, the name is RollbarErrorReporter.java
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

package org.maxgamer.quickshop.util.reporter.error;

import com.google.common.collect.Lists;
import com.rollbar.notifier.Rollbar;
import com.rollbar.notifier.config.Config;
import com.rollbar.notifier.config.ConfigBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.GameVersion;
import org.maxgamer.quickshop.util.ReflectFactory;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.paste.Paste;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.*;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class RollbarErrorReporter {
    //private volatile static String bootPaste = null;
    private final Rollbar rollbar;
    private final List<String> reported = new ArrayList<>(5);
    private final List<Class<?>> ignoredException = Lists.newArrayList(IOException.class
            , OutOfMemoryError.class
            , ProtocolException.class
            , InvalidPluginException.class
            , UnsupportedClassVersionError.class
            , LinkageError.class);
    private final QuickShop plugin;
    private final QuickShopExceptionFilter quickShopExceptionFilter;
    private boolean disable;
    private boolean tempDisable;
    private String lastPaste = null;
    private final GlobalExceptionFilter serverExceptionFilter;
    private final GlobalExceptionFilter globalExceptionFilter;
    @Getter
    private volatile boolean enabled = false;


    public RollbarErrorReporter(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        Config config = ConfigBuilder.withAccessToken("4846d9b99e5d4d238f9135ea9c744c28")
                .environment("release".contentEquals(QuickShop.getInstance().getBuildInfo().getGitBranch()) ? "production" : "development")
                .platform(plugin.getServer().getVersion())
                .codeVersion(QuickShop.getVersion())
                .handleUncaughtErrors(false)
                .build();
        this.rollbar = Rollbar.init(config);

        quickShopExceptionFilter = new QuickShopExceptionFilter(plugin.getLogger().getFilter());
        plugin.getLogger().setFilter(quickShopExceptionFilter); // Redirect log request passthrough our error catcher.

        serverExceptionFilter = new GlobalExceptionFilter(plugin.getServer().getLogger().getFilter());
        plugin.getServer().getLogger().setFilter(serverExceptionFilter);

        globalExceptionFilter = new GlobalExceptionFilter(Logger.getGlobal().getFilter());
        Logger.getGlobal().setFilter(globalExceptionFilter);

        Util.debugLog("Rollbar error reporter success loaded.");
//        if (bootPaste == null) {
//            new BukkitRunnable() {
//                @Override
//                public void run() {
//                    Paste paste = new Paste(plugin);
//                    lastPaste = paste.paste(paste.genNewPaste());
//                    if (lastPaste != null) {
//                        bootPaste = lastPaste;
//                        plugin.log("Plugin booted up, the server paste was created for debugging, reporting errors and data-recovery: " + lastPaste);
//                    }
//                }
//            }.runTaskAsynchronously(plugin);
//        } else {
//            plugin.log("Reload detected, the server paste will not created again, previous paste link: " + bootPaste);
//        }
        enabled = true;
    }

    public void unregister() {
        enabled = false;
        plugin.getLogger().setFilter(quickShopExceptionFilter.preFilter);
        plugin.getServer().getLogger().setFilter(serverExceptionFilter.preFilter);
        Logger.getGlobal().setFilter(globalExceptionFilter.preFilter);
    }

    private Map<String, Object> makeMapping() {
        Map<String, Object> dataMapping = new LinkedHashMap<>();
        dataMapping.put("paste", this.lastPaste);
        dataMapping.put("system_os", System.getProperty("os.name"));
        dataMapping.put("system_arch", System.getProperty("os.arch"));
        dataMapping.put("system_version", System.getProperty("os.version"));
        dataMapping.put("system_cores", String.valueOf(Runtime.getRuntime().availableProcessors()));
        dataMapping.put("server_build", plugin.getServer().getVersion());
        dataMapping.put("server_java", String.valueOf(System.getProperty("java.version")));
        dataMapping.put("server_players", plugin.getServer().getOnlinePlayers().size() + "/" + plugin.getServer().getMaxPlayers());
        dataMapping.put("server_onlinemode", String.valueOf(plugin.getServer().getOnlineMode()));
        dataMapping.put("server_bukkitversion", plugin.getServer().getVersion());

//        dataMapping.put("server_plugins", getPluginInfo());
        dataMapping.put("user", QuickShop.getInstance().getServerUniqueID().toString());
        return dataMapping;
    }

    private void sendError0(@NotNull Throwable throwable, @NotNull String... context) {
        try {
            if (plugin.getBootError() != null) {
                return; // Don't report any errors if boot failed.
            }
            if (tempDisable) {
                this.tempDisable = false;
                return;
            }
            if (disable) {
                return;
            }
            if (!enabled) {
                return;
            }

            if (!canReport(throwable)) {
                return;
            }
            if (ignoredException.contains(throwable.getClass())) {
                return;
            }
            if (throwable.getCause() != null) {
                if (ignoredException.contains(throwable.getCause())) {
                    return;
                }
            }
            if (lastPaste == null) {
                String pasteURL;
                try {
                    Paste paste = new Paste(plugin);
                    pasteURL = paste.paste(paste.genNewPaste());
                    if (pasteURL != null && !pasteURL.isEmpty()) {
                        lastPaste = pasteURL;
                    }
                } catch (Exception ex) {
                    // Ignore
                    pasteURL = this.lastPaste;
                }
            }
            this.rollbar.error(throwable, this.makeMapping(), throwable.getMessage());
            plugin
                    .getLogger()
                    .warning(
                            "A exception was thrown, QuickShop already caught this exception and reported it. This error will only shown once before next restart.");
            plugin.getLogger().warning("====QuickShop Error Report BEGIN===");
            plugin.getLogger().warning("Description: " + throwable.getMessage());
            plugin.getLogger().warning("Server   ID: " + plugin.getServerUniqueID());
            plugin.getLogger().warning("Exception  : ");
            ignoreThrows();
            throwable.printStackTrace();
            resetIgnores();
            plugin.getLogger().warning("====QuickShop Error Report E N D===");
            Util.debugLog(throwable.getMessage());
            Arrays.stream(throwable.getStackTrace()).forEach(a -> Util.debugLog(a.getClassName() + "." + a.getMethodName() + ":" + a.getLineNumber()));
            if (Util.isDevMode()) {
                throwable.printStackTrace();
            }
        } catch (Exception th) {
            ignoreThrow();
            plugin.getLogger().log(Level.WARNING, "Something going wrong when automatic report errors, please submit this error on Issue Tracker", th);
        }
    }

    /**
     * Send a error to Sentry
     *
     * @param throwable Throws
     * @param context   BreadCrumb
     */
    public void sendError(@NotNull Throwable throwable, @NotNull String... context) {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> sendError0(throwable, context));
            //ignore async response
        } else {
            sendError0(throwable, context);
        }
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
        if (plugin.getUpdateWatcher() == null) {
            return false;
        }
        if (!plugin.getUpdateWatcher().getUpdater().isLatest(plugin.getUpdateWatcher().getUpdater().getCurrentRunning())) { // We only receive latest reports.
            return false;
        }
        if (!GameVersion.get(ReflectFactory.getServerVersion()).isCoreSupports()) { // Ignore errors if user install quickshop on unsupported
            // version.
            return false;
        }

        PossiblyLevel possiblyLevel = checkWasCauseByQS(throwable);
        if (possiblyLevel == PossiblyLevel.IMPOSSIBLE) {
            return false;
        }
        if (throwable.getMessage().startsWith("#")) {
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
    public PossiblyLevel checkWasCauseByQS(@Nullable Throwable throwable) {
        if (throwable == null) {
            return PossiblyLevel.IMPOSSIBLE;
        }
        if (throwable.getMessage() == null) {
            return PossiblyLevel.IMPOSSIBLE;
        }
        if (throwable.getMessage().contains("Could not pass event")) {
            if (throwable.getMessage().contains("QuickShop")) {
                return PossiblyLevel.CONFIRM;
            } else {
                return PossiblyLevel.IMPOSSIBLE;
            }
        }
        while (throwable.getCause() != null) {
            throwable = throwable.getCause();
        }

        if (throwable.getStackTrace()[0].getClassName().contains("org.maxgamer.quickshop")) {
            return PossiblyLevel.CONFIRM;
        }
        long errorCount = Arrays.stream(throwable.getStackTrace())
                .limit(3)
                .filter(stackTraceElement -> stackTraceElement.getClassName().contains("org.maxgamer.quickshop"))
                .count();

        if (errorCount > 0) {
            return PossiblyLevel.MAYBE;
        } else if (throwable.getCause() != null) {
            return checkWasCauseByQS(throwable.getCause());
        }
        return PossiblyLevel.IMPOSSIBLE;
    }

    /**
     * Set ignore throw. It will unlocked after accept a throw
     */
    public void ignoreThrow() {
        tempDisable = true;
    }

    /**
     * Set ignore throws. It will unlocked after called method resetIgnores.
     */
    public void ignoreThrows() {
        disable = true;
    }

    /**
     * Reset ignore throw(s).
     */
    public void resetIgnores() {
        tempDisable = false;
        disable = false;
    }

    private String getPluginInfo() {
        StringBuilder buffer = new StringBuilder();
        for (Plugin bPlugin : plugin.getServer().getPluginManager().getPlugins()) {
            buffer
                    .append("\t")
                    .append(bPlugin.getName())
                    .append("@")
                    .append(bPlugin.isEnabled() ? "Enabled" : "Disabled")
                    .append("\n");
        }
        return buffer.toString();
    }

    enum PossiblyLevel {
        CONFIRM,
        MAYBE,
        IMPOSSIBLE
    }

    class GlobalExceptionFilter implements Filter {

        @Nullable
        @Getter
        private final Filter preFilter;

        GlobalExceptionFilter(@Nullable Filter preFilter) {
            this.preFilter = preFilter;
        }

        private boolean defaultValue(LogRecord record) {
            return preFilter == null || preFilter.isLoggable(record);
        }

        /**
         * Check if a given log record should be published.
         *
         * @param record a LogRecord
         * @return true if the log record should be published.
         */
        @Override
        public boolean isLoggable(@NotNull LogRecord record) {
            if (!enabled) {
                return defaultValue(record);
            }
            Level level = record.getLevel();
            if (level != Level.WARNING && level != Level.SEVERE) {
                return defaultValue(record);
            }
            if (record.getThrown() == null) {
                return defaultValue(record);
            }
            if (Util.isDevMode()) {
                sendError(record.getThrown(), record.getMessage());
                return defaultValue(record);
            } else {
                sendError(record.getThrown(), record.getMessage());
                PossiblyLevel possiblyLevel = checkWasCauseByQS(record.getThrown());
                if (possiblyLevel == PossiblyLevel.IMPOSSIBLE)
                    return true;
                if (possiblyLevel == PossiblyLevel.MAYBE) {
                    plugin.getLogger().warning("This seems not a QuickShop but we still sent this error to QuickShop developers. If you have any question, you should ask QuickShop developer.");
                    return true;
                }
                return false;
            }
        }

    }

    class QuickShopExceptionFilter implements Filter {
        @Nullable
        @Getter
        private final Filter preFilter;

        QuickShopExceptionFilter(@Nullable Filter preFilter) {
            this.preFilter = preFilter;
        }

        private boolean defaultValue(LogRecord record) {
            return preFilter == null || preFilter.isLoggable(record);
        }

        /**
         * Check if a given log record should be published.
         *
         * @param record a LogRecord
         * @return true if the log record should be published.
         */
        @Override
        public boolean isLoggable(@NotNull LogRecord record) {
            if (!enabled) {
                return defaultValue(record);
            }
            Level level = record.getLevel();
            if (level != Level.WARNING && level != Level.SEVERE) {
                return defaultValue(record);
            }
            if (record.getThrown() == null) {
                return defaultValue(record);
            }
            if (Util.isDevMode()) {
                sendError(record.getThrown(), record.getMessage());
                return defaultValue(record);
            } else {
                sendError(record.getThrown(), record.getMessage());
                PossiblyLevel possiblyLevel = checkWasCauseByQS(record.getThrown());
                if (possiblyLevel == PossiblyLevel.IMPOSSIBLE)
                    return true;
                if (possiblyLevel == PossiblyLevel.MAYBE) {
                    plugin.getLogger().warning("This seems not a QuickShop but we still sent this error to QuickShop developers. If you have any question, you should ask QuickShop developer.");
                    return true;
                }
                return false;
            }
        }

    }
}
