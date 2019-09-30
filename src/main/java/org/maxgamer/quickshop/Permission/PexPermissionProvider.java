package org.maxgamer.quickshop.Permission;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Util.Util;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;
@Deprecated
public class PexPermissionProvider implements PermissionProvider {
    /**
     * Test the sender has special permission
     *
     * @param sender     CommandSender
     * @param permission The permission want to check
     * @return hasPermission
     */
    @Override
    public boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        if (sender instanceof OfflinePlayer) {
            PermissionUser user = PermissionsEx.getUser(sender.getName());
            if (user == null) {
                return false;
            } else {
                return user.has(permission);
            }
        } else {
            return true;
        }
    }

    /**
     * Get permission provider name
     *
     * @return The name of permission provider
     */
    @Override
    public @NotNull String getName() {
        return "PermissionEx";
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
            return new PermissionInfomationContainer(sender, permission, null, "This user is Console");
        }
        PermissionUser user = PermissionsEx.getUser(sender.getName());
        if (user == null) {
            return new PermissionInfomationContainer(sender, permission, null, "User not exist.");
        } else {
            return new PermissionInfomationContainer(sender, permission, Util.array2String(user.getGroupsNames()), null);
        }
    }
}
