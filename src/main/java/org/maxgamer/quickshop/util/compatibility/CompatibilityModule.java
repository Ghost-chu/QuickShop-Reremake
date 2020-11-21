/*
 * This file is a part of project QuickShop, the name is CompatibilityModule.java
 *  Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util.compatibility;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.holder.QuickShopInstanceHolder;

public abstract class CompatibilityModule extends QuickShopInstanceHolder {
    public CompatibilityModule(QuickShop plugin) {
        super(plugin);
    }

    /**
     * Gets the CompatibilityModule provider name
     *
     * @return Provider name
     */
    public abstract @NotNull String getName();

    /**
     * Gets the CompatibilityModule provider plugin instance
     *
     * @return Provider Plugin instance
     */
    public abstract @NotNull Plugin getPlugin();

    /**
     * Calls CompatibilityModule to toggle the detection status for playerb between on and off
     *
     * @param player   The player
     * @param checking On or Off
     */
    public abstract void toggle(@NotNull Player player, boolean checking);
}
