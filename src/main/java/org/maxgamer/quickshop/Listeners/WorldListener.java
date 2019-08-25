package org.maxgamer.quickshop.Listeners;

import java.util.HashMap;
import java.util.Map.Entry;

import lombok.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopChunk;

@AllArgsConstructor
public class WorldListener implements Listener {
    private QuickShop plugin;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldLoad(WorldLoadEvent e) {
        /* *************************************
         * This listener fixes any broken world references. Such as hashmap
         * lookups will fail, because the World reference is different, but the
         * world value is the same.
         *  ************************************
         */

        World world = e.getWorld();
        plugin.getShopLoader().loadShops(world.getName());
        // New world data
        HashMap<ShopChunk, HashMap<Location, Shop>> inWorld = new HashMap<ShopChunk, HashMap<Location, Shop>>(1);
        // Old world data
        HashMap<ShopChunk, HashMap<Location, Shop>> oldInWorld = plugin.getShopManager().getShops(world.getName());
        // Nothing in the old world, therefore we don't care. No locations to
        // update.
        if (oldInWorld == null) {
            return;
        }
        for (Entry<ShopChunk, HashMap<Location, Shop>> oldInChunk : oldInWorld.entrySet()) {
            HashMap<Location, Shop> inChunk = new HashMap<Location, Shop>(1);
            // Put the new chunk were the old chunk was
            inWorld.put(oldInChunk.getKey(), inChunk);
            for (Entry<Location, Shop> entry : oldInChunk.getValue().entrySet()) {
                Shop shop = entry.getValue();
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
            HashMap<Location, Shop> inChunk = plugin.getShopManager().getShops(chunk);
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
        if (e.isCancelled()) {
            return;
        }
        // This is a workaround, because I don't get parsed chunk events when a
        // world unloads, I think...
        // So manually tell all of these shops they're unloaded.
        for (Chunk chunk : e.getWorld().getLoadedChunks()) {
            HashMap<Location, Shop> inChunk = plugin.getShopManager().getShops(chunk);
            if (inChunk == null || inChunk.isEmpty()) {
                continue;
            }
            for (Shop shop : inChunk.values()) {
                shop.onUnload();
            }
        }
    }
}