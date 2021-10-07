/*
 * This file is a part of project QuickShop, the name is IntegratedPlugin.java
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

package org.maxgamer.quickshop.api.integration;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.util.reload.Reloadable;

/**
 * Integration module
 */
public interface IntegratedPlugin extends Reloadable {
    /**
     * Return the integrated plugin name.
     * For example, Residence
     *
     * @return integrated plugin
     */
    @NotNull String getName();

    /**
     * Check if a player can create shop here
     *
     * @param player   the player want to create shop
     * @param location shop location
     * @return If you can create shop here
     */
    boolean canCreateShopHere(@NotNull Player player, @NotNull Location location);

    /**
     * Check if a player can trade with shop here
     *
     * @param player   the player want to trade with shop
     * @param location shop location
     * @return If you can trade with shop here
     */
    boolean canTradeShopHere(@NotNull Player player, @NotNull Location location);

    /**
     * Check if a player can delete a shop here
     *
     * @param player   the player want to delete the shop
     * @param location shop location
     * @return If you can delete the shop here
     */
    default boolean canDeleteShopHere(@NotNull Player player, @NotNull Location location) {
        return false;
    }

    /**
     * Loading logic
     * Execute Stage defined by IntegrationStage
     */
    void load();

    /**
     * Unloding logic
     * Will execute when Quickshop unloading
     */
    void unload();
}
