package org.maxgamer.quickshop.Listeners;

import lombok.AllArgsConstructor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;

import java.util.HashMap;

@AllArgsConstructor
public class ChunkListener implements Listener {
    private QuickShop plugin;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        if (e.isNewChunk()) {
            return;
        }
        HashMap<Location, Shop> inChunk = plugin.getShopManager().getShops(e.getChunk());
        if (inChunk == null || inChunk.isEmpty()) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                //noinspection unchecked
                for (Shop shop : ((HashMap<Location,Shop>)inChunk.clone()).values()) {
                    shop.onLoad();
                }
                //Delay 1 tick, hope can fix the magic bug in 1.14 spigot build.
            }
        }.runTaskLater(plugin, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent e) {
        if (ListenerHelper.isDisabled(e.getClass())) {
            return;
        }
        Chunk c = e.getChunk();
        HashMap<Location, Shop> inChunk = plugin.getShopManager().getShops(c);
        if (inChunk == null || inChunk.isEmpty()) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                //noinspection unchecked
                for (Shop shop : ((HashMap<Location,Shop>)inChunk.clone()).values()) {
                    shop.onUnload();
                }
            }
        }.runTaskLater(plugin, 1); //Delay 1 tick, hope can fix the magic bug in 1.14 spigot build.
    }
}