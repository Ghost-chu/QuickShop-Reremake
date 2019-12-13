/*
 * This file is a part of project QuickShop, the name is WorldListener.java
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
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopChunk;

import java.util.HashMap;
import java.util.Map.Entry;

@AllArgsConstructor
public class WorldListener implements Listener {

    @NotNull
    private final QuickShop plugin;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent e) {
        /* *************************************
         * This listener fixes any broken world references. Such as hashmap
         * lookups will fail, because the World reference is different, but the
         * world value is the same.
         *  ************************************
         */
        final World world = e.getWorld();

        plugin.getShopLoader().loadShops(world.getName());
        // New world data
        final HashMap<ShopChunk, HashMap<Location, Shop>> inWorld = new HashMap<>(1);
        // Old world data
        final HashMap<ShopChunk, HashMap<Location, Shop>> oldInWorld = plugin.getShopManager().getShops(world.getName());
        // Nothing in the old world, therefore we don't care. No locations to
        // update.
        if (oldInWorld == null) {
            return;
        }

        for (Entry<ShopChunk, HashMap<Location, Shop>> oldInChunk : oldInWorld.entrySet()) {
            final HashMap<Location, Shop> inChunk = new HashMap<>(1);
            // Put the new chunk were the old chunk was
            inWorld.put(oldInChunk.getKey(), inChunk);

            for (Entry<Location, Shop> entry : oldInChunk.getValue().entrySet()) {
                final Shop shop = entry.getValue();

                shop.getLocation().setWorld(world);
                inChunk.put(shop.getLocation(), shop);
            }
        }
        // Done - Now we can store the new world dataz!
        plugin.getShopManager().getShops().put(world.getName(), inWorld);
        // This is a workaround, because I don't get parsed chunk events when a
        // world first loads....
        // So manually tell all of these shops they're loaded.
        for (Chunk chunk : world.getLoadedChunks()) {
            final HashMap<Location, Shop> inChunk = plugin.getShopManager().getShops(chunk);

            if (inChunk == null || inChunk.isEmpty()) {
                continue;
            }

            for (Shop shop : inChunk.values()) {
                shop.onLoad();
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent e) {
        // FIXME: 24/11/2019 It's not necessary but ok.
        // This is a workaround, because I don't get parsed chunk events when a
        // world unloads, I think...
        // So manually tell all of these shops they're unloaded.
        for (Chunk chunk : e.getWorld().getLoadedChunks()) {
            final HashMap<Location, Shop> inChunk = plugin.getShopManager().getShops(chunk);

            if (inChunk == null || inChunk.isEmpty()) {
                continue;
            }

            for (Shop shop : inChunk.values()) {
                shop.onUnload();
            }
        }
    }
}