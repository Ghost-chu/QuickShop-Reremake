package org.maxgamer.quickshop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * A class to check known issue cause plugin failed enable.
 */
class BuiltInSolution {
    /**
     * Call when failed load economy system, and use this to check the reason.
     *
     * @return The reason of error.
     */
    static BootError econError() {
        // Check Vault is installed
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            // Vault not installed
            return new BootError("Vault is not installed or loaded!", "Make sure you installed Vault.");
        }
        // Vault is installed
        if (Bukkit.getPluginManager().getPlugin("CMI") != null) {
            // Found may in-compatiable plugin
            return new BootError("No Economy plugin loaded", "Make sure you have an economy plugin hooked into Vault.", ChatColor.YELLOW + "Incompatibility detected: CMI Installed", "Download CMI Edition of Vault might fix this.");
        }
        return new BootError("No Economy plugin loaded", "Install an economy plugin to get Vault working.");
    }

    /**
     * Call when failed load database, and use this to check the reason.
     *
     * @return The reason of error.
     */
    static BootError databaseError() {
        return new BootError("Error connecting to the database", "Make sure your database service is running.", "Or check the configuration in your config.yml");
    }
}
