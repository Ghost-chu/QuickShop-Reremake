package org.maxgamer.quickshop.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
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
import org.json.simple.JSONObject;
import org.maxgamer.quickshop.QuickShop;

/**
 * Auto report errors to qs's sentry.
 */
public class SentryErrorReporter {
    private SentryClient sentryClient;
    private Context context;
    private final String dsn = "https://9a64b22513544155b32d302392a46564@sentry.io/1473041?" + "stacktrace.app.packages=&async=true";
    private boolean enabled;
    private final ArrayList<String> reported = new ArrayList<>();
    private QuickShop plugin;

    public SentryErrorReporter(QuickShop plugin) {
        this.plugin = plugin;
        JSONObject serverData = plugin.getMetrics().getServerData();
        //sentryClient = Sentry.init(dsn);
        Util.debugLog("Loading SentryErrorReporter");
        sentryClient = SentryClientFactory.sentryClient(this.dsn);
        context = sentryClient.getContext();
        Util.debugLog("Setting basic report data...");
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
        context.setUser(new UserBuilder().setId(QuickShop.getUniqueID().toString()).build());
        sentryClient.setServerName(Bukkit.getServer().getName() + " @ " + Bukkit.getServer().getVersion());
        sentryClient.setRelease(QuickShop.getVersion());
        sentryClient.setEnvironment(Util.isDevEdition() ? "Development" : "Production");
        plugin.getLogger().setFilter(new QuickShopExceptionFilter()); //Redirect log request passthrough our error catcher.
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
    public void unit() {
        enabled = false;
    }

    private String getPluginInfo() {
        StringBuilder buffer = new StringBuilder();
        for (Plugin bplugin : Bukkit.getPluginManager().getPlugins()) {
            buffer.append("\t" + bplugin.getName() + "@" + (bplugin.isEnabled() ? "Enabled" : "Disabled") + "\n");
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
    public UUID sendError(Throwable throwable, String... context) {
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
                .warning("A exception was throwed, QuickShop already caught this exception and reported, switch to debug mode to get full errors.");
        plugin.getLogger().warning("====QuickShop Error Report BEGIN===");
        plugin.getLogger().warning("Description: " + throwable.getMessage());
        plugin.getLogger().warning("Event    ID: " + this.context.getLastEventId().toString());
        plugin.getLogger().warning("Server   ID: " + QuickShop.getUniqueID().toString());
        plugin.getLogger().warning("====QuickShop Error Report E N D===");
        plugin.getLogger().warning("Copy report and send to author to help us fix bugs.");
        return this.context.getLastEventId();
    }

    /**
     * Check a throw is cause by QS
     * @param throwable Throws
     * @return Cause or not
     */
    private boolean checkWasCauseByQS(Throwable throwable) {
        StackTraceElement[] stackTraces = throwable.getStackTrace();
        Optional<StackTraceElement> element;
        element = Arrays.stream(throwable.getStackTrace())
                .limit(5)
                .filter(stackTraceElement -> stackTraceElement.getClassName().contains("org.maxgamer.quickshop"))
                .findFirst();
        if (!element.isPresent())
            return false;

        // for (StackTraceElement stackTraceElement : stackTraces) {
        //     boolean byqs = stackTraceElement.getClassName().contains("org.maxgamer.quickshop");
        //     if (byqs) {
        //         return true;
        //     }
        // }
        //Not found.
        return true;

    }

    /**
     * Dupe report check
     * @param throwable Throws
     * @return dupecated
     */
    private boolean canReport(Throwable throwable) {
        if (!enabled) {
            return false;
        }
        String text = throwable.getMessage() + "%" + throwable.getStackTrace()[0];
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
        public boolean isLoggable(LogRecord record) {
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
            //There wasn't need check from who, it just directly report it.
            if (Util.isDevMode()) {
                return true;
            }
            sendError(record.getThrown(), record.getMessage());
            return false; //Hide errors
        }
    }

}
