/*
 * This file is a part of project QuickShop, the name is LockListener.java
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

package org.maxgamer.quickshop.listener;

import io.papermc.lib.PaperLib;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Cache;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

public class LockListener extends ProtectionListenerBase {

    public LockListener(@NotNull final QuickShop plugin, @Nullable final Cache cache) {
        super(plugin, cache);
    }

    /*
     * Removes chests when they're destroyed.
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        BlockState state = PaperLib.getBlockState(b, false).getState();
        if (state instanceof Sign) {
            final Sign sign = (Sign) state;
            if (sign.getLine(0).equals(super.getPlugin().getConfig().getString("lockette.private"))
                    || sign.getLine(0).equals(super.getPlugin().getConfig().getString("lockette.more_users"))) {
                // Ignore break lockette sign
                Util.debugLog("Skipped a dead-lock shop sign.(Lockette or other sign-lock plugin)");
                return;
            }
        }

        final Player p = e.getPlayer();
        // If the chest was a chest
        if (Util.canBeShop(b)) {
            final Shop shop = getShopPlayer(b.getLocation(), true);

            if (shop == null) {
                return; // Wasn't a shop
            }
            // If they owned it or have bypass perms, they can destroy it
            if (!shop.getOwner().equals(p.getUniqueId())
                    && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.destroy")) {
                e.setCancelled(true);
                MsgUtil.sendMessage(p, "no-permission", p);
            }
        } else if (Util.isWallSign(b.getType())) {
            if (b instanceof Sign) {
                final Sign sign = (Sign) b;

                if (sign.getLine(0).equals(super.getPlugin().getConfig().getString("lockette.private"))
                        || sign.getLine(0).equals(super.getPlugin().getConfig().getString("lockette.more_users"))) {
                    // Ignore break lockette sign
                    return;
                }
            }
            b = Util.getAttached(b);

            if (b == null) {
                return;
            }

            final Shop shop = getShopPlayer(b.getLocation(), false);

            if (shop == null) {
                return;
            }
            // If they're the shop owner or have bypass perms, they can destroy
            // it.
            if (!shop.getOwner().equals(p.getUniqueId())
                    && !QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.destroy")) {
                e.setCancelled(true);
                MsgUtil.sendMessage(p, "no-permission", p);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClick(PlayerInteractEvent e) {

        final Block b = e.getClickedBlock();

        if (b == null) {
            return;
        }

        if (!Util.canBeShop(b)) {
            return;
        }

        final Player p = e.getPlayer();

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return; // Didn't right click it, we dont care.
        }

        final Shop shop = getShopPlayer(b.getLocation(), true);
        // Make sure they're not using the non-shop half of a double chest.
        if (shop == null) {
            return;
        }

        if (!shop.getModerator().isModerator(p.getUniqueId())) {
            if (QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.open")) {
                MsgUtil.sendMessage(p, "bypassing-lock", p);
                return;
            }
            MsgUtil.sendMessage(p, "that-is-locked", p);
            e.setCancelled(true);
        }
    }

    /*
     * Handles hopper placement
     */
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {

        final Block b = e.getBlock();

        if (b.getType() != Material.HOPPER) {
            return;
        }

        final Player p = e.getPlayer();

        if (!Util.isOtherShopWithinHopperReach(b, p)) {
            return;
        }

        if (QuickShop.getPermissionManager().hasPermission(p, "quickshop.other.open")) {
            MsgUtil.sendMessage(p, "bypassing-lock", p);
            return;
        }

        MsgUtil.sendMessage(p, "that-is-locked", p);
        e.setCancelled(true);
    }

}
