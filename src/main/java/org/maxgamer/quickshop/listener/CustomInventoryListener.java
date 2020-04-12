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

package org.maxgamer.quickshop.listener;

import lombok.AllArgsConstructor;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.holder.QuickShopPreviewInventoryHolder;

@AllArgsConstructor
public class CustomInventoryListener implements Listener {

    @NotNull
    private final QuickShop plugin;

    @EventHandler(ignoreCancelled = true)
    public void invEvent(InventoryInteractEvent e) {
        if(e.getInventory().getHolder() instanceof QuickShopPreviewInventoryHolder){
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void invEvent(InventoryMoveItemEvent e) {
        if(e.getDestination().getHolder() instanceof QuickShopPreviewInventoryHolder){
            e.setCancelled(true);
            return;
        }
        if(e.getSource() instanceof QuickShopPreviewInventoryHolder){
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void invEvent(InventoryClickEvent e) {
        if(e.getInventory().getHolder() instanceof QuickShopPreviewInventoryHolder){
            e.setCancelled(true);
            e.setResult(Result.DENY);
        }
    }

    @EventHandler
    public void invEvent(InventoryDragEvent e) {
        if(e.getInventory().getHolder() instanceof QuickShopPreviewInventoryHolder){
            e.setCancelled(true);
            e.setResult(Result.DENY);
        }
    }
}
