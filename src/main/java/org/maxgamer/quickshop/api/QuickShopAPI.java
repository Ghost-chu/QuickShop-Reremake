/*
 * This file is a part of project QuickShop, the name is QuickShopAPI.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.api;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandManager;
import org.maxgamer.quickshop.integration.IntegrationHelper;
import org.maxgamer.quickshop.util.compatibility.CompatibilityManager;
import org.maxgamer.quickshop.util.matcher.item.ItemMatcher;

public class QuickShopAPI {
    private static QuickShop plugin;
    private static ShopAPI shopAPI;
    private static DisplayItemAPI displayItemAPI;

    public static void _internal_access_only_setupApi(@NotNull JavaPlugin quickshop) {
        if (!(quickshop instanceof QuickShop)) {
            throw new IllegalArgumentException("You can't setup API, it should only access by QuickShop internal calling.");
        }
        plugin = (QuickShop) quickshop;
        shopAPI = new ShopAPI(plugin);
        displayItemAPI = new DisplayItemAPI(plugin);
    }

    /**
     * Gets apis about shop
     *
     * @return The Shop API
     */
    public static @Nullable ShopAPI getShopAPI() {
        return shopAPI;
    }

    /**
     * Gets apis about display item
     *
     * @return The DisplayItem API
     */
    public static @Nullable DisplayItemAPI getDisplayItemAPI() {
        return displayItemAPI;
    }

    /**
     * Gets anti-cheat compatibility manager to allow you access and process.
     * If you calling this before plugin loaded up, you might get nothing.
     *
     * @return Compatibility Manager
     */
    public static @Nullable CompatibilityManager getCompatibilityManager() {
        return plugin.getCompatibilityTool();
    }

    /**
     * Gets protection plugins integration helper to allow hook your plugin to our checks system.
     * If you calling this before plugin loaded up, you might get nothing.
     *
     * @return IntegrationHelper
     */
    public static @Nullable IntegrationHelper getIntegrationManager() {
        return plugin.getIntegrationHelper();
    }

    /**
     * Gets command manager to allow you access quickshop command system
     *
     * @return CommandManager
     */
    public static @Nullable CommandManager getCommandManager() {
        return plugin.getCommandManager();
    }

    /**
     * Gets ItemMatcher instance to allow using the same matcher that quickshop using to matching the items.
     *
     * @return ItemMatcher
     */
    public static @Nullable ItemMatcher getItemMatcher() {
        return plugin.getItemMatcher();
    }
}
