package org.maxgamer.quickshop.Permission;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class VaultPermissionProvider implements PermissionProvider {
    private Permission api;

    public VaultPermissionProvider() {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            throw new ProviderIsEmptyException(getName());
        }
        api = rsp.getProvider();
    }

    /**
     * Test the sender has special permission
     *
     * @param sender     CommandSender
     * @param permission The permission want to check
     * @return hasPermission
     */
    @Override
    public boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        return api.has(sender, permission);
    }

    /**
     * Get permission provider name
     *
     * @return The name of permission provider
     */
    @Override
    public @NotNull String getName() {
        return "Vault";
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
        if (sender instanceof Server) {
            return new PermissionInfomationContainer(sender, permission, null, "User is Console");
        }
        OfflinePlayer offlinePlayer = (OfflinePlayer) sender;
        return new PermissionInfomationContainer(sender, permission, api.getPrimaryGroup(null, offlinePlayer), null);
    }
}
