/*
 * This file is a part of project QuickShop, the name is PermissionChecker.java
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

package org.maxgamer.quickshop.util;

import com.griefcraft.lwc.LWCPlugin;
import com.griefcraft.model.Protection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.event.ProtectionCheckStatus;
import org.maxgamer.quickshop.event.ShopProtectionCheckEvent;
import org.primesoft.blockshub.BlocksHubBukkit;

import java.util.concurrent.atomic.AtomicBoolean;

public class PermissionChecker {
    private final QuickShop plugin;

    private final boolean usePermissionChecker;

    public PermissionChecker(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        usePermissionChecker = this.plugin.getConfig().getBoolean("shop.protection-checking");
    }

    /**
     * Check player can build in target location
     *
     * @param player   Target player
     * @param location Target location
     * @return Success
     */
    public boolean canBuild(@NotNull Player player, @NotNull Location location) {
        return canBuild(player, location.getBlock());
    }

    /**
     * Check player can build in target block
     *
     * @param player Target player
     * @param block  Target block
     * @return Success
     */
    public boolean canBuild(@NotNull Player player, @NotNull Block block) {

        if (plugin.getLwcPlugin() != null) {
            LWCPlugin lwc = (LWCPlugin) plugin.getLwcPlugin();
            Protection protection = lwc.getLWC().findProtection(block.getLocation());
            if (protection != null) {
                if (!protection.isOwner(player)) {
                    Util.debugLog("LWC reporting player no permission to access this block.");
                    return false;
                }
            }

        }

        if (plugin.getBlockHubPlugin() != null) {
            BlocksHubBukkit blocksHubBukkit = (BlocksHubBukkit) plugin.getBlockHubPlugin();
            boolean bhCanBuild = blocksHubBukkit.getApi().hasAccess(player.getUniqueId(), blocksHubBukkit.getApi().getWorld(block.getWorld().getName()), block.getX(), block.getY(), block.getZ());
            if (plugin.getConfig().getBoolean("plugin.BlockHub.only")) {
                Util.debugLog("BlockHub only mode response: " + bhCanBuild);
                return bhCanBuild;
            } else {
                if (!bhCanBuild) {
                    Util.debugLog("BlockHub reporting player no permission to access this region.");
                    return false;
                }
            }
        }
        if (!usePermissionChecker) {
            return true;
        }
        final AtomicBoolean isCanBuild = new AtomicBoolean(false);

        BlockBreakEvent beMainHand;
        // beMainHand = new BlockPlaceEvent(block, block.getState(), block.getRelative(0, -1, 0),
        // player.getInventory()
        // getItemInMainHand(), player, true, EquipmentSlot.HAND);

        beMainHand = new BlockBreakEvent(block, player);
        // Call for event for protection check start
        Bukkit.getPluginManager()
                .callEvent(
                        new ShopProtectionCheckEvent(
                                block.getLocation(), player, ProtectionCheckStatus.BEGIN, beMainHand));
        beMainHand.setDropItems(false);
        beMainHand.setExpToDrop(0);

        //register a listener to cancel test event
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.HIGHEST)
            public void onTestEvent(BlockBreakEvent event) {
                if (event == beMainHand) {
                    // Call for event for protection check end
                    Bukkit.getPluginManager().callEvent(
                            new ShopProtectionCheckEvent(
                                    block.getLocation(), player, ProtectionCheckStatus.END, beMainHand));
                    if (!event.isCancelled()) {
                        //Ensure this test will no be logged by some plugin
                        beMainHand.setCancelled(true);
                        isCanBuild.set(true);
                    }
                    HandlerList.unregisterAll(this);
                }
            }
        }, plugin);

        Bukkit.getPluginManager().callEvent(beMainHand);

        return isCanBuild.get();
    }

}
