package org.maxgamer.quickshop.Permission;

import org.anjocaido.groupmanager.GroupManager;
import org.anjocaido.groupmanager.permissions.AnjoPermissionsHandler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;

@Deprecated
public class GroupManagerProvider implements PermissionProvider {
    private GroupManager groupManager;

    @Deprecated
    public GroupManagerProvider() throws ProviderIsEmptyException {
        Plugin gmPlugin = Bukkit.getPluginManager().getPlugin("GroupManager");
        if (gmPlugin == null || !gmPlugin.isEnabled()) {
            throw new ProviderIsEmptyException(getName());
        }
        try {
            groupManager = (GroupManager) gmPlugin;
        } catch (Throwable th) {
            QuickShop.instance.getSentryErrorReporter().ignoreThrow();
            th.printStackTrace();
            QuickShop.instance.getLogger().warning("Failed hook into GroupManager, maybe you using unsupported version. We will fallback to other permission providers, if you got any trouble, [DO NOT REPORT TO QUICKSHOP], uninstall GroupManager and use LuckPerms.");
            throw new ProviderIsEmptyException(getName());
        }
        QuickShop.instance.getLogger().warning("Compatibility warning: Many users reported GroupManager not works fine with Vault and QuickShop, I can't promise this patch is works well so if you got any problem, [DO NOT REPORT TO QUICKSHOP] uninstall the GroupManager and use LuckPerms.");

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
        if (sender instanceof Server) {
            return true;
        }
        if (((OfflinePlayer) sender).isOnline()) {
            AnjoPermissionsHandler handler = groupManager.getWorldsHolder().getWorldPermissions((Player) sender);
            if (handler == null) {
                return false;
            }
            return handler.has((Player) sender, permission);
        } else {
            //Stupid GroupManager doesn't support OfflinePlayer permissions checking.
            return false;
        }
    }

    /**
     * Get permission provider name
     *
     * @return The name of permission provider
     */
    @Override
    public @NotNull String getName() {
        return "GroupManager";
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
            return new PermissionInfomationContainer(sender, permission, null, "This user is Console.");
        }
        if (!((OfflinePlayer) sender).isOnline()) {
            return new PermissionInfomationContainer(sender, permission, null, "GroupManager doesn't support OfflinePlayer permissions checking.");
        } else {
            AnjoPermissionsHandler handler = groupManager.getWorldsHolder().getWorldPermissions((Player) sender);
            if (handler == null) {
                return new PermissionInfomationContainer(sender, permission, null, "Permission Handler is empty");
            }
            StringBuilder permissionBuilder = new StringBuilder();
            handler.getAllPlayersPermissions(sender.getName(), true).forEach((node) -> permissionBuilder.append(node).append("\n"));
            return new PermissionInfomationContainer(sender, permission, handler.getPrimaryGroup(sender.getName()), "This player have permissions: \n" + permissionBuilder.toString() + "\nWARNING: GroupManager doesn't have support, don't report to us if you got any troubles.");
        }
    }
}
