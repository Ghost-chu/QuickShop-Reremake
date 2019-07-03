package org.maxgamer.quickshop.Util;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.*;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import io.sentry.context.Context;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.UserBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.*;
import org.json.simple.JSONObject;
import org.maxgamer.quickshop.QuickShop;

/**
 * Auto report errors to qs's sentry.
 */
public class SentryErrorReporter {
    private Context context;
    private final String dsn = "https://6a7881aa07164412bcb84a0f76253ae9@sentry.io/1473041?" + "stacktrace.app.packages=org.maxgamer.quickshop";
    /* Pre-init it if it called before the we create it... */
    private SentryClient sentryClient;
    private boolean enabled;
    private final ArrayList<String> reported = new ArrayList<>();
    private QuickShop plugin;
    private boolean tempDisable;
    private boolean disable;
    private List<Class> ignoredException = new ArrayList<>();

    public SentryErrorReporter(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        //sentryClient = Sentry.init(dsn);
        Util.debugLog("Loading SentryErrorReporter");
        sentryClient = SentryClientFactory.sentryClient(this.dsn);
        context = sentryClient.getContext();
        Util.debugLog("Setting basic report data...");
        JSONObject serverData = plugin.getMetrics().getPluginData();
        //context.addTag("plugin_version", QuickShop.getVersion());
        context.addTag("system_os", String.valueOf(serverData.get("osName")));
        context.addTag("system_arch", String.valueOf(serverData.get("osArch")));
        context.addTag("system_version", String.valueOf(serverData.get("osVersion")));
        context.addTag("system_cores", String.valueOf(serverData.get("coreCount")));
        context.addTag("server_build", String.valueOf(Bukkit.getServer().getVersion()));
        context.addTag("server_java", String.valueOf(serverData.get("javaVersion")));
        context.addTag("server_players", String
                .valueOf(serverData.get("playerAmount") + "/" + Bukkit.getOfflinePlayers().length));
        context.addTag("server_onlinemode", String.valueOf(serverData.get("onlineMode")));
        context.addTag("server_bukkitversion", String.valueOf(serverData.get("bukkitVersion")));
        context.addTag("server_plugins", getPluginInfo());
        context.setUser(new UserBuilder().setId(plugin.getServerUniqueID().toString()).build());
        sentryClient.setServerName(Bukkit.getServer().getName() + " @ " + Bukkit.getServer().getVersion());
        sentryClient.setRelease(QuickShop.getVersion());
        sentryClient.setEnvironment(Util.isDevEdition() ? "Development" : "Production");
        plugin.getLogger().setFilter(new QuickShopExceptionFilter()); //Redirect log request passthrough our error catcher.
        //Bukkit.getLogger().setFilter(new GlobalExceptionFilter());
        /* Ignore we won't report errors */
        ignoredException.add(IOException.class);
        ignoredException.add(OutOfMemoryError.class);
        ignoredException.add(ProtocolException.class);

        Util.debugLog("Enabled!");
        enabled = true;
        if (!plugin.getConfig().getBoolean("auto-report-errors")) {
            Util.debugLog("Disabled!'");
            unit();
        }
    }

    /**
     * Unload Sentry error reporter
     */
    private void unit() {
        enabled = false;
    }

    private String getPluginInfo() {
        StringBuilder buffer = new StringBuilder();
        for (Plugin bplugin : Bukkit.getPluginManager().getPlugins()) {
            buffer.append("\t").append(bplugin.getName()).append("@").append(bplugin.isEnabled() ? "Enabled" : "Disabled")
                    .append("\n");
        }
        return buffer.toString();
    }

    /**
     * Send a error to Sentry
     *
     * @param throwable Throws
     * @param context   BreadCrumb
     * @return Event Uniqud ID
     */
    private UUID sendError(@NotNull Throwable throwable, @NotNull String... context) {
        if (tempDisable) {
            Util.debugLog("Ignore a throw, cause this throw flagged not reporting.");
            this.tempDisable = true;
            return null;
        }
        if (disable) {
            Util.debugLog("Ignore a throw, cause report now is disabled.");
            this.disable = true;
            return null;
        }
        Util.debugLog("Preparing for reporting errors...");
        if (!enabled) {
            Util.debugLog("Errors not sended, cause ErrorReport not enabled.");
            return null;
        }

        if (!checkWasCauseByQS(throwable)) {
            Util.debugLog("Errors not sended, cause it not throw by QuickShop");
            return null;
        }
        if (!canReport(throwable)) {
            Util.debugLog("This errors not sended, cause it disallow send.(Already sended?)");
            return null;
        }

        for (String record : context) {
            this.context.recordBreadcrumb(
                    new BreadcrumbBuilder().setMessage(record).build()
            );
        }
        Paste paste = new Paste(plugin);
        String pasteURL = "Failed to paste.";
        try {
            pasteURL = paste.pasteTheText(paste.genNewPaste());
        } catch (Throwable ex) {
            //Ignore
        }
        this.context.addTag("paste", pasteURL);
        this.sentryClient.sendException(throwable);
        this.context.clearBreadcrumbs();
        plugin.getLogger()
                .warning("A exception was thrown, QuickShop already caught this exception and reported it, switch to debug mode to see the full errors.");
        plugin.getLogger().warning("====QuickShop Error Report BEGIN===");
        plugin.getLogger().warning("Description: " + throwable.getMessage());
        plugin.getLogger().warning("Event    ID: " + this.context.getLastEventId().toString());
        plugin.getLogger().warning("Server   ID: " + plugin.getServerUniqueID().toString());
        plugin.getLogger().warning("====QuickShop Error Report E N D===");
        return this.context.getLastEventId();
    }

    /**
     * Check a throw is cause by QS
     * @param throwable Throws
     * @return Cause or not
     */
    private boolean checkWasCauseByQS(@Nullable Throwable throwable) {
        if (throwable == null)
            return false;
        Optional<StackTraceElement> element = Arrays.stream(throwable.getStackTrace())
                .limit(1)
                .filter(stackTraceElement -> stackTraceElement.getClassName().contains("org.maxgamer.quickshop"))
                .findFirst();
        return element.isPresent();
    }

    /**
     * Dupe report check
     * @param throwable Throws
     * @return dupecated
     */
    private boolean canReport(@NotNull Throwable throwable) {
        if (!enabled) {
            return false;
        }
        StackTraceElement stackTraceElement;
        if (throwable.getStackTrace().length < 3) {
            stackTraceElement = throwable.getStackTrace()[1];
        } else {
            stackTraceElement = throwable.getStackTrace()[2];
        }
        String text = stackTraceElement.getClassName() + "#" + stackTraceElement.getMethodName() + "#" + stackTraceElement
                .getLineNumber();
        if (!reported.contains(text)) {
            reported.add(text);
            return true;
        } else {
            return false;
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
                //We didn't care normal logs.
                return true;
            }
            /* Check stupid Sentry's Warning*/
            if (record.getMessage().contains("stacktrace.app.packages"))
                return false;
            if (record.getThrown() == null) {
                //We didn't care warnings/errors for non-exception.
                return true;
            }
            //There wasn't need check from who, it just directly report it.

            //Do not reporting when it is develop env.
            if (Util.isDevMode()) {
                return true;
            }
            //No, pls do not report the OutOfMemory Error, i didn't care it.

            if (ignoredException.contains(record.getThrown().getClass()))
                return true;
            sendError(record.getThrown(), record.getMessage());
            return false; //Hide errors
        }
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
                //We didn't care normal logs.
                return true;
            }

            if (record.getThrown() == null) {
                //We didn't care warnings/errors for non-exception.
                return true;
            }
            /* Check is it cause by QS */
            if (!checkWasCauseByQS(record.getThrown()))
                return true;

            //Do not reporting when it is develop env.
            if (Util.isDevMode()) {
                return true;
            }
            //No, pls do not report the OutOfMemory Error, i didn't care it.
            if (ignoredException.contains(record.getThrown().getClass()))
                return true;
            sendError(record.getThrown(), record.getMessage());
            return false; //Hide errors
        }
    }

    /**
     * Set ignore throw.
     * It will unlocked after accept a throw
     */
    public void ignoreThrow() {
        tempDisable = true;
    }

    /**
     * Set ignore throws.
     * It will unlocked after called method resetIgnores.
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

}
