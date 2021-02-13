/*
 * This file is a part of project QuickShop, the name is SyncTaskWatcher.java
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

package org.maxgamer.quickshop.watcher;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.QuickShop;

import java.util.LinkedList;
import java.util.Queue;

//TODO: This is stupid, need a better impl
public class SyncTaskWatcher {
    @Getter
    private final Queue<Entity> entityRemoveQueue = new LinkedList<>();

    @Getter
    private final Queue<InventoryEditContainer> inventoryEditQueue = new LinkedList<>();

    @Getter
    private final Queue<ItemStack> itemStackRemoveQueue = new LinkedList<>();
    // private QuickShop plugin;

    /**
     * SyncTaskWatcher is a loop task runner, it can be add from async thread and run in Bukkit main
     * thread.
     *
     * @param plugin QuickShop instance
     */
    public SyncTaskWatcher(QuickShop plugin) {
        // this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            if (entityRemoveQueue.isEmpty()) {
                return;
            }
            Entity entity = entityRemoveQueue.poll();
            while (entity != null) {
                entity.remove();
                entity = entityRemoveQueue.poll();
            }
            if (itemStackRemoveQueue.isEmpty()) {
                return;
            }
            ItemStack itemStack = itemStackRemoveQueue.poll();
            while (itemStack != null) {
                //itemStack.setAmount(0);
                itemStack.setType(Material.AIR);
                itemStack = itemStackRemoveQueue.poll();
            }
            if (inventoryEditQueue.isEmpty()) {
                return;
            }
            InventoryEditContainer container = inventoryEditQueue.poll();
            while (container != null) {
                container.getInventory().setItem(container.getSlot(), container.getNewItemStack());
                container = inventoryEditQueue.poll();
            }

        }, 0, 5);
    }
}
