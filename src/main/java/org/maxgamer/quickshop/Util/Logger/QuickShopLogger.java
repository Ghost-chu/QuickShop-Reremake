/*
 * This file is a part of project QuickShop, the name is QuickShopLogger.java
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

package org.maxgamer.quickshop.Util.Logger;

import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.BLACK;
import static org.bukkit.ChatColor.BLUE;
import static org.bukkit.ChatColor.BOLD;
import static org.bukkit.ChatColor.DARK_AQUA;
import static org.bukkit.ChatColor.DARK_BLUE;
import static org.bukkit.ChatColor.DARK_GRAY;
import static org.bukkit.ChatColor.DARK_GREEN;
import static org.bukkit.ChatColor.DARK_PURPLE;
import static org.bukkit.ChatColor.DARK_RED;
import static org.bukkit.ChatColor.GOLD;
import static org.bukkit.ChatColor.GRAY;
import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.ITALIC;
import static org.bukkit.ChatColor.LIGHT_PURPLE;
import static org.bukkit.ChatColor.MAGIC;
import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.RESET;
import static org.bukkit.ChatColor.STRIKETHROUGH;
import static org.bukkit.ChatColor.UNDERLINE;
import static org.bukkit.ChatColor.WHITE;
import static org.bukkit.ChatColor.YELLOW;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;
import org.fusesource.jansi.Ansi;

/*
 * Originally take from Mypet which is a awesome project,
 * extends PluginLogger in order to replace the default logger.
 *
 * This is generally a new built logger system that
 * rely on the Java logger to provide better customization.
 *
 * It is possible to switch to Log4j for better performance in the future.
 */
public class QuickShopLogger extends PluginLogger {

  protected boolean debugSetup = false;

  /** Mapping from the text pattern of Bukkit color to the corresponding text format of Ansi */
  private Map<Pattern, String> bukkitToAnsi;

  // below are non-static for secret optimization
  /** Regex that indicates the case insensitive */
  private String IGNORE_CASE;

  // private FileHandler debugLogFileHandler = null;
  private boolean AnsiSupported = true;

  @SneakyThrows
  public QuickShopLogger(Plugin plugin) {
    super(plugin);

    // Ansi setup
    try {
      if (Ansi.isEnabled()) registerStyles();
    } catch (NoClassDefFoundError e) {
      AnsiSupported = false;
      info("Your server doesn't support ANSI color, disabled color formatter.");
    }

    // Logger re-naming
    String prefix = plugin.getDescription().getPrefix();
    String pluginName =
        prefix != null
            ? "[" + ChatColor.YELLOW + prefix + ChatColor.RESET + "] "
            : "[" + ChatColor.YELLOW + plugin.getDescription().getName() + ChatColor.RESET + "] ";
    pluginName = AnsiSupported ? applyStyles(pluginName) : pluginName;

    // Apply plugin name for BukkitLogger
    Field pluginNameField = PluginLogger.class.getDeclaredField("pluginName");
    pluginNameField.setAccessible(true); // private
    pluginNameField.set(this, pluginName);

    // Remove logger name from package name
    Field nameField = Logger.class.getDeclaredField("name");
    nameField.setAccessible(true); // private
    nameField.set(this, "");

    this.config();
    // super.setUseParentHandlers(false);
  }

  // Logging stuffs
  @Override
  public void log(LogRecord logRecord) {
    String message = logRecord.getMessage();

    if (message != null && AnsiSupported) {
      if (logRecord.getLevel() == Level.WARNING) {
        message = ChatColor.YELLOW + message;
      }
      if (logRecord.getLevel() == Level.SEVERE) {
        message = ChatColor.RED + message;
      }
      message = applyStyles(message);
      logRecord.setMessage(message);
    }
    super.log(logRecord);
  }

  /**
   * Collect params as a string with blank spaces between
   *
   * @param params
   * @return collected string
   */
  public String collectParams(Object... params) {
    return Arrays.stream(params).map(String::valueOf).collect(Collectors.joining(" "));
  }

  public void info(Object... params) {
    this.info(collectParams(params));
  }

  public void warning(Object... params) {
    this.warning(collectParams(params));
  }

  public void severe(Object... params) {
    this.severe(collectParams(params));
  }

  public void config(Object... params) {
    this.config(collectParams(params));
  }

  public void fine(Object... params) {
    this.fine(collectParams(params));
  }

  public void finer(Object... params) {
    this.finer(collectParams(params));
  }

  public void finest(Object... params) {
    this.finest(collectParams(params));
  }

  // Style stuffs

  /**
   * Apply Ansi styples to the specific message if it contains, internally converting Bukkit style
   * color text to Ansi code.
   *
   * @param message to apply styles
   * @return text maybe applied styles
   */
  public String applyStyles(String message) {
    for (Entry<Pattern, String> entry : bukkitToAnsi.entrySet())
      message = entry.getKey().matcher(message).replaceAll(entry.getValue());

    return message.concat(Ansi.ansi().reset().toString());
  }

  /*
   * This will compile patterns for Bukkit colors as the key,
   * and stringify Ansi as the value, then register in to the system.
   */
  private void registerStyles() {
    // Initial here for secert optimization
    bukkitToAnsi = new HashMap<Pattern, String>();
    IGNORE_CASE = "(?i)";

    // Colors
    regAnsiMapping(BLACK, Ansi.Color.BLACK);
    regAnsiMapping(DARK_BLUE, Ansi.Color.BLUE);
    regAnsiMapping(DARK_GREEN, Ansi.Color.GREEN);
    regAnsiMapping(DARK_AQUA, Ansi.Color.CYAN);
    regAnsiMapping(DARK_RED, Ansi.Color.RED);
    regAnsiMapping(DARK_PURPLE, Ansi.Color.MAGENTA);
    regAnsiMapping(GOLD, Ansi.Color.YELLOW);
    regAnsiMapping(GRAY, Ansi.Color.WHITE);
    regAnsiMapping(DARK_GRAY, Ansi.Color.BLACK, true);
    regAnsiMapping(BLUE, Ansi.Color.BLUE, true);
    regAnsiMapping(GREEN, Ansi.Color.GREEN, true);
    regAnsiMapping(AQUA, Ansi.Color.CYAN, true);
    regAnsiMapping(RED, Ansi.Color.RED, true);
    regAnsiMapping(LIGHT_PURPLE, Ansi.Color.MAGENTA, true);
    regAnsiMapping(YELLOW, Ansi.Color.YELLOW, true);
    regAnsiMapping(WHITE, Ansi.Color.WHITE, true);

    // Effects
    regAnsiMapping(MAGIC, Ansi.Attribute.BLINK_SLOW);
    regAnsiMapping(BOLD, Ansi.Attribute.UNDERLINE_DOUBLE);
    regAnsiMapping(STRIKETHROUGH, Ansi.Attribute.STRIKETHROUGH_ON);
    regAnsiMapping(UNDERLINE, Ansi.Attribute.UNDERLINE);
    regAnsiMapping(ITALIC, Ansi.Attribute.ITALIC);
    regAnsiMapping(RESET, Ansi.Attribute.RESET);
  }

  /*
   * Register a mapping from Bukkit color to Ansi
   */
  private void regAnsiMapping(ChatColor bukkColor, Ansi.Color ansiColor) {
    regAnsiMapping0(toPattern(bukkColor), toDesc(ansiColor));
  }

  /*
   * Register a mapping from Bukkit color to Ansi
   */
  private void regAnsiMapping(ChatColor bukkColor, Ansi.Attribute ansiAttribute) {
    regAnsiMapping0(toPattern(bukkColor), toDesc(ansiAttribute));
  }

  /*
   * Register a mapping from Bukkit color to Ansi, with color option
   */
  private void regAnsiMapping(ChatColor bukkColor, Ansi.Color ansiColor, boolean intensity) {
    regAnsiMapping0(toPattern(bukkColor), toDesc(ansiColor, intensity));
  }

  /*
   * Register a mapping from the pattern of Bukkit color to the description of Ansi,
   * and this is the genuine type for them to be registered.
   */
  private void regAnsiMapping0(Pattern bukkitPattern, String ansiDesc) {
    this.bukkitToAnsi.put(bukkitPattern, ansiDesc);
  }

  /**
   * Convert a Bukkit color to regex pattern
   *
   * @param bukkitColor the bukkit color
   * @return the pattern
   */
  private Pattern toPattern(ChatColor bukkitColor) {
    return Pattern.compile(IGNORE_CASE.concat(bukkitColor.toString()));
  }

  /**
   * To populate a Ansi with a reset attribute ahead
   *
   * @param ansiColor
   * @return Ansi with reset ahead
   */
  private Ansi resetWith(Ansi.Color ansiColor) {
    return Ansi.ansi().a(Ansi.Attribute.RESET).fg(ansiColor);
  }

  /**
   * Convert a Ansi to its description text
   *
   * @param ansiColor Ansi
   * @param intensity Ansi color option
   * @return stringified Ansi
   */
  private String toDesc(Ansi.Color ansiColor, boolean intensity) {
    return intensity ? resetWith(ansiColor).bold().toString() : toDesc(ansiColor);
  }

  /**
   * Convert a Ansi to its description text
   *
   * @param ansiColor Ansi
   * @return stringified Ansi
   */
  private String toDesc(Ansi.Color ansiColor) {
    return resetWith(ansiColor).boldOff().toString();
  }

  /**
   * Convert a Ansi to its description text
   *
   * @param ansiAttribute Ansi
   * @return stringified Ansi
   */
  private String toDesc(Ansi.Attribute ansiAttribute) {
    return Ansi.ansi().a(Ansi.Attribute.RESET).toString();
  }
}
