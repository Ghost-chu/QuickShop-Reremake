/*
 * This file is a part of project QuickShop, the name is PermissionManager.java
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
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

@Getter
public class PermissionManager {
    private QuickShop plugin;
    private PermissionProvider provider;

    /**
     * The manager to call permission providers
     *
     * @param plugin Instance
     */
    public PermissionManager(QuickShop plugin) {
        this.plugin = plugin;
        provider = new BukkitPermsProvider();
        plugin.getLogger().info("Selected permission provider: " + provider.getName());
    }

    /**
     * Check the permission for sender
     *
     * @param sender     The CommandSender you want check
     * @param permission The permission node wait to check
     * @return The result of check
     */
    public boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission) {
        try {
            boolean result = provider.hasPermission(sender, permission);
            if (Util.isDevMode()) {
                try {
                    PermissionInfomationContainer container = provider.getDebugInfo(sender, permission);
                    Util.debugLog("=======");
                    Util.debugLog("Result: " + result);
                    Util.debugLog("Sender: " + container.getSender().getName());
                    Util.debugLog("Permission Node: " + container.getPermission());
//                    Util.debugLog("Primary Group: " + container.getGroupName());
//                    Util.debugLog("Other infos: " + container.getOtherInfos());
                } catch (Throwable th) {
                    th.printStackTrace();
                    Util.debugLog("Exception throwed when getting debug messages.");
                }
            }
            return result;
        } catch (Throwable th) {
            plugin.getSentryErrorReporter().ignoreThrow();
            th.printStackTrace();
            plugin.getLogger().info("A error happend, if you believe this is QuickShop problem, please report to us on Issue Tracker or Discord.");
            return false;
        }
    }
}
