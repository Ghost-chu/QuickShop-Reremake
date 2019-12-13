/*
 * This file is a part of project QuickShop, the name is LuckPermsProvider.java
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

package org.maxgamer.quickshop.Permission;

import lombok.Getter;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Util.Util;

import java.util.Objects;

@Getter
@Deprecated
public class LuckPermsProvider implements PermissionProvider {
    private PermissionManager manager;
    private LuckPermsApi api;

    @Deprecated
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
                return Objects.requireNonNull(Objects.requireNonNull(api.getUser(((OfflinePlayer) sender).getUniqueId()))).hasPermission(api.buildNode(permission).build()).asBoolean();
            } catch (Exception npe) {
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
        return new PermissionInfomationContainer(sender, permission, user.getPrimaryGroup(), "This player all permissions: \n" + permissionsBuilder);
    }
}
