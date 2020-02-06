/*
 * This file is a part of project QuickShop, the name is BuiltInSolution.java
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

package org.maxgamer.quickshop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * A class to check known issue cause plugin failed enable.
 */
class BuiltInSolution {
    private static QuickShop plugin = QuickShop.instance;

    /**
     * Call when failed load database, and use this to check the reason.
     *
     * @return The reason of error.
     */
    static BootError databaseError() {
        return new BootError(
                "Error connecting to the database",
                "Make sure your database service is running.",
                "Or check the configuration in your config.yml");
    }

    /**
     * Call when failed load economy system, and use this to check the reason.
     *
     * @return The reason of error.
     */
    static BootError econError() {
        // Check Vault is installed
        if (Bukkit.getPluginManager().getPlugin("Vault") == null
                && Bukkit.getPluginManager().getPlugin("Reserve") == null) {
            // Vault not installed
            return new BootError(
                    "Vault or Reserve is not installed or loaded!",
                    "Make sure you installed Vault or Reserve.");
        }
        // if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
        //     // Vault not installed
        //     return new BootError("Vault is not installed or loaded!", "Make sure you installed
        // Vault.");
        // }
        // Vault is installed
        if (Bukkit.getPluginManager().getPlugin("CMI") != null) {
            // Found may in-compatiable plugin
            return new BootError(
                    "No Economy plugin detected, did you installed and loaded them? Make sure they loaded before QuickShop.",
                    "Make sure you have an economy plugin hooked into Vault or Reserve.",
                    ChatColor.YELLOW + "Incompatibility detected: CMI Installed",
                    "Download CMI Edition of Vault might fix this.");
        }

        return new BootError(
                "No Economy plugin detected, did you installed and loaded them? Make sure they loaded before QuickShop.",
                "Install an economy plugin to get Vault or Reserve working.");
    }
}
