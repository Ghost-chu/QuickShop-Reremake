package org.maxgamer.quickshop.Permission;

import lombok.Getter;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Util.Util;

import java.io.Console;

@Getter
public class LuckPermsProvider implements PermissionProvider {
    private PermissionManager manager;
    private LuckPermsApi api;

    public LuckPermsProvider(PermissionManager manager) throws ProviderIsEmptyException {
        this.manager = manager;
        try {
            api = LuckPerms.getApi();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            throw new ProviderIsEmptyException("LuckPerms");
        }

    }

    @Override
    public boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        if (sender instanceof OfflinePlayer) {
            try {
                return api.getUser(((OfflinePlayer) sender).getUniqueId()).hasPermission(api.buildNode(permission).build()).asBoolean();
            } catch (NullPointerException npe) {
                Util.debugLog("Failed to get user " + sender.getName() + " 's LuckPerms permission infomation, return failed.");
                return false;
            }
        }
        return true;
    }


    @NotNull
    @Override
    public String getName() {
        return "LuckPerms";
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
            return new PermissionInfomationContainer(sender, permission, null, "This sender is console.");
        }
        OfflinePlayer player = (OfflinePlayer) sender;
        User user = api.getUser(player.getUniqueId());
        if (user == null) {
            return new PermissionInfomationContainer(sender, permission, null, "User not exist.");
        }
        StringBuilder permissionsBuilder = new StringBuilder();
        user.getPermissions().forEach((pnode) -> permissionsBuilder.append(pnode.getPermission()).append("\n"));
        return new PermissionInfomationContainer(sender, permission, user.getPrimaryGroup(), "This player all permissions: \n" + permissionsBuilder.toString());
    }
}
