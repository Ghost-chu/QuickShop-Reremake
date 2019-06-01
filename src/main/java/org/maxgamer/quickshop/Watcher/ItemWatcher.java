package org.maxgamer.quickshop.Watcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.QueueAction;
import org.maxgamer.quickshop.Shop.QueueShopObject;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopChunk;

/**
 * @author Netherfoam Maintains the display items, restoring them when needed.
 * Also deletes invalid items.
 */
public class ItemWatcher implements Runnable {
    private QuickShop plugin;

    public ItemWatcher(QuickShop plugin) {
        this.plugin = plugin;
    }

    public void run() {
        List<Shop> toRemove = new ArrayList<>(1);
        for (Entry<String, HashMap<ShopChunk, HashMap<Location, Shop>>> inWorld : plugin.getShopManager().getShops().entrySet()) {
            // This world
            World world = Bukkit.getWorld(inWorld.getKey());
            if (world == null)
                continue; // world not loaded.
            for (Entry<ShopChunk, HashMap<Location, Shop>> inChunk : inWorld.getValue().entrySet()) {
                if (!world.isChunkLoaded(inChunk.getKey().getX(), inChunk.getKey().getZ())) {
                    // If the chunk is not loaded, next chunk!
                    continue;
                }
                for (Shop shop : inChunk.getValue().values()) {
                    // Validate the shop.
                    if (!shop.isValid()) {
                        toRemove.add(shop);
                    }
                }
            }
        }
        // Now we can remove it.
        for (Shop shop : toRemove) {
            new QueueShopObject(shop, QueueAction.DELETE);
        }
    }
}