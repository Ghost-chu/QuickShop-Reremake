/*
 * This file is a part of project QuickShop, the name is CustomInventoryListener.java
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

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.holder.QuickShopPreviewGUIHolder;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

public class CustomInventoryListener extends AbstractQSListener {

    public CustomInventoryListener(QuickShop plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void invEvent(InventoryInteractEvent e) {
        if (e.getInventory().getHolder() instanceof QuickShopPreviewGUIHolder) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void invEvent(InventoryMoveItemEvent e) {
        if (e.getDestination().getHolder() instanceof QuickShopPreviewGUIHolder || e.getSource().getHolder() instanceof QuickShopPreviewGUIHolder) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void invEvent(InventoryClickEvent e) {
        if (e.getInventory().getHolder() instanceof QuickShopPreviewGUIHolder) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void invEvent(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof QuickShopPreviewGUIHolder) {
            e.setCancelled(true);
        }
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}
