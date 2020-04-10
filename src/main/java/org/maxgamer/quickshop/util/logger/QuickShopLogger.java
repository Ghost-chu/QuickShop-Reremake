/*
 * This file is a part of project QuickShop, the name is QuickShopLogger.java Copyright (C)
 * Ghost_chu <https://github.com/Ghost-chu> Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.util.logger;
//
//import lombok.SneakyThrows;
//import org.bukkit.ChatColor;
//import org.bukkit.plugin.Plugin;
//import org.bukkit.plugin.PluginLogger;
//
//import java.lang.reflect.Field;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.logging.Level;
//import java.util.logging.LogRecord;
//import java.util.logging.Logger;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//import static org.bukkit.ChatColor.*;
//
///*
// * Originally take from Mypet which is a awesome project, extends PluginLogger in order to replace
// * the default logger.
// *
// * This is generally a new built logger system that rely on the Java logger to provide better
// * customization.
// */
//@Deprecated
//public class QuickShopLogger extends PluginLogger {
//    protected boolean debugSetup = false;
//    /**
//     * Mapping from the text pattern of Bukkit color to the corresponding text format of Ansi
//     */
//    private Map<Pattern, String> bukkitToAnsi;
//    // below are non-static for secret optimization
//    /**
//     * Regex that indicates the case insensitive
//     */
//    private String IGNORE_CASE;
//    // private FileHandler debugLogFileHandler = null;
//    private boolean hasAnsi;
//
//    private boolean hasJline;
//
//    @SneakyThrows
//    @Deprecated
//    public QuickShopLogger(Plugin plugin) {
//        super(plugin);
//        registerStyles();
//
//        // Logger re-naming
//        String prefix = plugin.getDescription().getPrefix();
//        String pluginName = (prefix != null ? "[" + ChatColor.YELLOW + prefix + ChatColor.RESET + "] "
//                : "[" + ChatColor.YELLOW + plugin.getDescription().getName() + ChatColor.RESET + "] ");
//        pluginName = applyStyles(pluginName);
//
//            // Remove logger name from package name
//            Field nameField = Logger.class.getDeclaredField("name");
//            nameField.setAccessible(true); // private
//            nameField.set(this, "");
//
//        // Apply plugin name for BukkitLogger
//        Field pluginNameField = PluginLogger.class.getDeclaredField("pluginName");
//        pluginNameField.setAccessible(true); // private
//        pluginNameField.set(this, pluginName);
//
//        // Ansi setup
//        try {
//            hasAnsi = org.fusesource.jansi.Ansi.isEnabled();
//        } catch (NoClassDefFoundError e) {
//            hasAnsi = false;
//            info("Your server do not support Ansi, colour formatter will not be applied.");
//        }
//
//        Class<?> main = Class.forName("org.bukkit.craftbukkit.Main"); // Not in subversion
//        Field useJline = main.getField("useJline");
//        hasJline = useJline.getBoolean(null);
//        if (!hasJline) {
//            info("As you have turned Jline off, colour formatter will not be applied.");
//        }
//
//        this.config();
//        // super.setUseParentHandlers(false);
//    }
//
//    // Logging stuffs
//    @Override
//    @Deprecated
//    public void log(LogRecord logRecord) {
//        String message = logRecord.getMessage();
//
//        if (message != null) {
//
//
//                if (logRecord.getLevel() == Level.WARNING) {
//                    message = ChatColor.YELLOW + message;
//                } else if (logRecord.getLevel() == Level.SEVERE) {
//                    message = ChatColor.RED + message;
//                }
//
//                logRecord.setMessage(applyStyles(message));
//                super.log(logRecord);
//
//        }
//    }
//
//    /**
//     * Apply Ansi styples to the specific message if it contains, internally converting Bukkit style
//     * color text to Ansi code or empty string if Ansi is not available.
//     *
//     * @param message to apply styles
//     * @return text maybe applied styles
//     */
//    @Deprecated
//    public String applyStyles(String message) {
//        for (Entry<Pattern, String> entry : bukkitToAnsi.entrySet()) {
//            message =
//                entry.getKey().matcher(message).replaceAll(hasAnsi && hasJline ? entry.getValue() : "");
//        }
//
//        return hasAnsi && hasJline ? message.concat(org.fusesource.jansi.Ansi.ansi().reset().toString())
//            : message;
//    }
//    @Deprecated
//    public void info(Object... params) {
//            super.info(collectParams(params));
//    }
//
//    /**
//     * Collect params as a string with blank spaces between
//     *
//     * @param params Params
//     * @return collected string
//     */
//    @Deprecated
//    public String collectParams(Object... params) {
//        return Arrays.stream(params).map(String::valueOf).collect(Collectors.joining(" "));
//    }
//    @Deprecated
//    public void warning(Object... params) {
//
//            super.warning(collectParams(params));
//    }
//    @Deprecated
//    public void severe(Object... params) {
//
//            super.severe(collectParams(params));
//
//    }
//    @Deprecated
//    public void config(Object... params) {
//
//            super.config(collectParams(params));
//
//    }
//    @Deprecated
//    public void fine(Object... params) {
//
//            super.fine(collectParams(params));
//
//    }
//    @Deprecated
//    public void finer(Object... params) {
//
//            super.finer(collectParams(params));
//
//    }
//    @Deprecated
//    public void finest(Object... params) {
//
//            super.finest(collectParams(params));
//
//    }
//
//    /*
//     * This will compile patterns for Bukkit colors as the key, and stringify Ansi as the value, then
//     * register in to the system.
//     */
//    private void registerStyles() {
//        // Initial here for secert optimization
//        bukkitToAnsi = new HashMap<>();
//        IGNORE_CASE = "(?i)";
//
//        // Colors
//        regAnsiMapping(BLACK, !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.BLACK);
//        regAnsiMapping(DARK_BLUE, !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.BLUE);
//        regAnsiMapping(DARK_GREEN,
//            !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.GREEN);
//        regAnsiMapping(DARK_AQUA, !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.CYAN);
//        regAnsiMapping(DARK_RED, !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.RED);
//        regAnsiMapping(DARK_PURPLE,
//            !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.MAGENTA);
//        regAnsiMapping(GOLD, !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.YELLOW);
//        regAnsiMapping(GRAY, !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.WHITE);
//        regAnsiMapping(DARK_GRAY, !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.BLACK,
//            true);
//        regAnsiMapping(BLUE, !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.BLUE,
//            true);
//        regAnsiMapping(GREEN, !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.GREEN,
//            true);
//        regAnsiMapping(AQUA, !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.CYAN,
//            true);
//        regAnsiMapping(RED, !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.RED, true);
//        regAnsiMapping(LIGHT_PURPLE,
//            !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.MAGENTA, true);
//        regAnsiMapping(YELLOW, !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.YELLOW,
//            true);
//        regAnsiMapping(WHITE, !(hasAnsi && hasJline) ? null : org.fusesource.jansi.Ansi.Color.WHITE,
//            true);
//
//        // Effects
//        regAnsiMapping(MAGIC, org.fusesource.jansi.Ansi.Attribute.BLINK_SLOW);
//        regAnsiMapping(BOLD, org.fusesource.jansi.Ansi.Attribute.UNDERLINE_DOUBLE);
//        regAnsiMapping(STRIKETHROUGH, org.fusesource.jansi.Ansi.Attribute.STRIKETHROUGH_ON);
//        regAnsiMapping(UNDERLINE, org.fusesource.jansi.Ansi.Attribute.UNDERLINE);
//        regAnsiMapping(ITALIC, org.fusesource.jansi.Ansi.Attribute.ITALIC);
//        regAnsiMapping(RESET, org.fusesource.jansi.Ansi.Attribute.RESET);
//    }
//
//    /*
//     * Register a mapping from Bukkit color to Ansi
//     */
//    private void regAnsiMapping(ChatColor bukkColor, org.fusesource.jansi.Ansi.Color ansiColor) {
//        regAnsiMapping0(toPattern(bukkColor), toDesc(ansiColor));
//    }
//
//    /*
//     * Register a mapping from Bukkit color to Ansi
//     */
//    private void regAnsiMapping(ChatColor bukkColor,
//                                org.fusesource.jansi.Ansi.Attribute ansiAttribute) {
//        regAnsiMapping0(toPattern(bukkColor), toDesc(ansiAttribute));
//    }
//
//    /*
//     * Register a mapping from Bukkit color to Ansi, with color option
//     */
//    private void regAnsiMapping(ChatColor bukkColor, org.fusesource.jansi.Ansi.Color ansiColor,
//                                boolean intensity) {
//        regAnsiMapping0(toPattern(bukkColor), toDesc(ansiColor, intensity));
//    }
//
//    /*
//     * Register a mapping from the pattern of Bukkit color to the description of Ansi, and this is the
//     * genuine type for them to be registered.
//     */
//    private void regAnsiMapping0(Pattern bukkitPattern, String ansiDesc) {
//        this.bukkitToAnsi.put(bukkitPattern, ansiDesc);
//    }
//
//    /**
//     * Convert a Bukkit color to regex pattern
//     *
//     * @param bukkitColor the bukkit color
//     * @return the pattern
//     */
//    private Pattern toPattern(ChatColor bukkitColor) {
//        return Pattern.compile(IGNORE_CASE.concat(bukkitColor.toString()));
//    }
//
//    /**
//     * To populate a Ansi with a reset attribute ahead
//     *
//     * @param ansiColor the Ansi need to add reset attribute
//     * @return Ansi with reset ahead
//     */
//    private org.fusesource.jansi.Ansi resetWith(org.fusesource.jansi.Ansi.Color ansiColor) {
//        return org.fusesource.jansi.Ansi.ansi().a(org.fusesource.jansi.Ansi.Attribute.RESET)
//            .fg(ansiColor);
//    }
//
//    /**
//     * Convert a Ansi to its description text
//     *
//     * @param ansiColor Ansi
//     * @param intensity Ansi color option
//     * @return stringified Ansi
//     */
//    private String toDesc(org.fusesource.jansi.Ansi.Color ansiColor, boolean intensity) {
//        return hasAnsi && hasJline
//            ? (intensity ? resetWith(ansiColor).bold().toString() : toDesc(ansiColor))
//            : "";
//    }
//
//    /**
//     * Convert a Ansi to its description text
//     *
//     * @param ansiColor Ansi
//     * @return stringified Ansi
//     */
//    private String toDesc(org.fusesource.jansi.Ansi.Color ansiColor) {
//        return hasAnsi && hasJline ? resetWith(ansiColor).boldOff().toString() : "";
//    }
//
//    /**
//     * Convert a Ansi to its description text
//     *
//     * @param ansiAttribute Ansi
//     * @return stringified Ansi
//     */
//    private String toDesc(org.fusesource.jansi.Ansi.Attribute ansiAttribute) {
//        return hasAnsi && hasJline
//            ? org.fusesource.jansi.Ansi.ansi().a(org.fusesource.jansi.Ansi.Attribute.RESET).toString()
//            : "";
//    }
//
//}
