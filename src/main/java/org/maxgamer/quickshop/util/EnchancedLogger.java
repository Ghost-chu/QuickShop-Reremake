/*
 * This file is a part of project QuickShop, the name is EnchancedLogger.java
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

package org.maxgamer.quickshop.util;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLogger;

import java.util.logging.Level;

public class EnchancedLogger extends PluginLogger {

    public EnchancedLogger(Plugin plugin) {
        super(plugin);
    }

    /**
     * Log a message, with an array of object arguments.
     * <p>
     * If the logger is currently enabled for the given message
     * level then a corresponding LogRecord is created and forwarded
     * to all the registered output Handler objects.
     *
     * @param level  One of the message level identifiers, e.g., SEVERE
     * @param msg    The string message (or a key in the message catalog)
     * @param params array of parameters to the message
     */
    @Override
    public void log(Level level, String msg, Object... params) {
        super.log(level, msg, new Object[]{params});
    }

    public void info(String msg, Object... params) {
        super.log(Level.INFO, msg, params);
    }

    public void warning(String msg, Object... params) {
        super.log(Level.WARNING, msg, params);
    }

    public void severe(String msg, Object... params) {
        super.log(Level.SEVERE, msg, params);
    }
}
