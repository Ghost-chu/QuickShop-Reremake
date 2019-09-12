package org.maxgamer.quickshop.Watcher;

import java.util.*;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopChunk;
import org.maxgamer.quickshop.Util.Util;

/**
 * Check the shops after server booted up, make sure shop can correct self-deleted when container lost.
 */
public class ShopVaildWatcher extends BukkitRunnable {
    private QuickShop plugin;
    private Queue<Shop> checkQueue = new LinkedList<>();

    public ShopVaildWatcher(@NotNull QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int checkedShops = 0;
        int maxCheckShops = plugin.getConfig().getInt("shop.max-shops-checks-in-once");
        Shop shop = checkQueue.poll();
        while (shop != null){
            if(shop.isLoaded() && !shop.isValid()){
                shop.delete();
                Util.debugLog("Removed shop at "+shop.getLocation()+" cause the container is missing or not a usable container.");
            }
            checkedShops ++;
            if(checkedShops >= maxCheckShops){
                Util.debugLog("Shop check reached the limit, force exit and wait next check window.");
            }
            shop = checkQueue.poll();
        }
    }
}