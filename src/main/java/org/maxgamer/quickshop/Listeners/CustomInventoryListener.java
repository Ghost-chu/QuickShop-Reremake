/*
 * This file is a part of project QuickShop, the name is CustomInventoryListener.java
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

package org.maxgamer.quickshop.Listeners;

import lombok.AllArgsConstructor;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.InventoryPreview;

@AllArgsConstructor
public class CustomInventoryListener implements Listener {

    @NotNull
    private final QuickShop plugin;

    @EventHandler(ignoreCancelled = true)
    public void invEvent(InventoryInteractEvent e) {

        final Inventory inventory = e.getInventory();
        final ItemStack[] stacks = inventory.getContents();

        for (ItemStack itemStack : stacks) {
            if (!InventoryPreview.isPreviewItem(itemStack)) {
                continue;
            }

            e.setCancelled(true);
            e.setResult(Result.DENY);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void invEvent(InventoryMoveItemEvent e) {

        if (InventoryPreview.isPreviewItem(e.getItem())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void invEvent(InventoryClickEvent e) {

        if (InventoryPreview.isPreviewItem(e.getCursor())) {
            e.setCancelled(true);
            e.setResult(Result.DENY);
        }

        if (InventoryPreview.isPreviewItem(e.getCurrentItem())) {
            e.setCancelled(true);
            e.setResult(Result.DENY);
        }
    }

    @EventHandler
    public void invEvent(InventoryDragEvent e) {

        if (InventoryPreview.isPreviewItem(e.getCursor())
            || InventoryPreview.isPreviewItem(e.getOldCursor())) {
            e.setCancelled(true);
            e.setResult(Result.DENY);
        }
    }

    @EventHandler
    public void invEvent(InventoryPickupItemEvent e) {

        final Inventory inventory = e.getInventory();
        final ItemStack[] stacks = inventory.getContents();

        for (ItemStack itemStack : stacks) {
            if (!InventoryPreview.isPreviewItem(itemStack)) {
                continue;
            }

            e.setCancelled(true);
        }
    }

}
