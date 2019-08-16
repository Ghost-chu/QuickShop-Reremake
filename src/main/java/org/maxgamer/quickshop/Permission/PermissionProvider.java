package org.maxgamer.quickshop.Permission;

import org.bukkit.command.CommandSender;

public interface PermissionProvider {
    /**
     * Test the sender has special permission
     * @param sender CommandSender
     * @return hasPermission
     */
    boolean hasPermission(CommandSender sender);

    /**
     * Get permission provider name
     * @return The name of permission provider
     */
    String getName();
}
