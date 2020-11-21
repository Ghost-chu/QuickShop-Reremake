/*
 * This file is a part of project QuickShop, the name is LockListener.java
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

package org.maxgamer.quickshop.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Cache;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

public class PluginListener extends QSListener {

    public PluginListener(@NotNull final QuickShop plugin) {
        super(plugin);
    }

    /*
     * Disable Spartan integration when plugin is disabled
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPluginDisable(PluginDisableEvent e) {
       if(e.getPlugin().getName().equals("Spartan")){
           plugin.getCompatibilityTool().unregister(plugin.getSpartanCompatibilityModule());
           plugin.getLogger().info("Spartan compatibility integration was disabled, because plugin got disabled.");
       }
    }

    /*
     * Enable Spartan integration when plugin is enabled
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onPluginEnable(PluginEnableEvent e) {
        if(e.getPlugin().getName().equals("Spartan")){
            plugin.getCompatibilityTool().register(plugin.getSpartanCompatibilityModule());
            plugin.getLogger().info("Spartan compatibility integration was enabled, because plugin got enabled.");
        }
    }
}
