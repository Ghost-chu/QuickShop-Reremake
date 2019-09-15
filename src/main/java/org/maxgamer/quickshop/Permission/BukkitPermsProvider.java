package org.maxgamer.quickshop.Permission;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class BukkitPermsProvider implements PermissionProvider {

    @Override
    public boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        return sender.hasPermission(permission);
    }

    @Override
    public @NotNull String getName() {
        return "Bukkit";
    }

    /**
     * Get the debug infos in provider
     *
     * @param sender     CommandSender
     * @param permission The permission want to check
     * @return Debug Infos
     */
    @Override
    public @NotNull PermissionInfomationContainer getDebugInfo(@NotNull CommandSender sender, @NotNull String permission) {
        return new PermissionInfomationContainer(sender, permission, null, null);
    }
}
