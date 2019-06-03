package org.maxgamer.quickshop.Listeners;

import java.util.HashMap;
import java.util.Map;

import lombok.*;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.QueueAction;
import org.maxgamer.quickshop.Shop.QueueShopObject;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopChunk;
import org.maxgamer.quickshop.Util.Util;

@AllArgsConstructor
public class ChunkListener implements Listener {
    private QuickShop plugin;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent e) {
        Chunk c = e.getChunk();
        Map<String, HashMap<ShopChunk, HashMap<Location, Shop>>> map = plugin.getShopManager().getShops();
        if (plugin.getShopManager().getShops() == null || map.isEmpty())
            return;
        HashMap<Location, Shop> inChunk = plugin.getShopManager().getShops(c);
        if (inChunk == null || inChunk.isEmpty())
            return;
        new BukkitRunnable() {

            @Override
            public void run() {
                for (Shop shop : inChunk.values()) {
                    Util.debugLog("Add shop at " + shop.getLocation().toString() + " to waiting load queue.");
                    plugin.getQueuedShopManager()
                            .add(new QueueShopObject(shop, QueueAction.LOAD, QueueAction.SETSIGNTEXT));
                }

            }
        }.runTaskLater(plugin, 1); //Delay 1 tick, hope can fix the magic bug in 1.14 spigot build.
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent e) {
        Chunk c = e.getChunk();
        HashMap<Location, Shop> inChunk = plugin.getShopManager().getShops(c);
        if (inChunk == null || inChunk.isEmpty())
            return;
        for (Shop shop : inChunk.values()) {
            plugin.getQueuedShopManager().add(new QueueShopObject(shop, QueueAction.UNLOAD));
        }
    }
}