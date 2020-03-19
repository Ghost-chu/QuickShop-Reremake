package org.maxgamer.quickshop;

import java.util.Map;
import java.util.WeakHashMap;
import org.bukkit.Location;
import org.maxgamer.quickshop.Shop.Shop;

public class Cache {
    private QuickShop plugin;

    private long expireTime = 1000;

    private Map<Location, Shop> accessCaching = new WeakHashMap<>(50);

    private Map<Location, Long> cachingExpire = new WeakHashMap<>(50);

    public Cache(QuickShop plugin) {
        this.plugin = plugin;
    }

    public boolean hasValidCache(Location block) {
        Long time = cachingExpire.get(block);
        if (time == null) {
            return false;
        }
        return time <= expireTime;
    }

    public Shop getCachingShop(Location block) {
        return accessCaching.getOrDefault(block, null);
    }

    public void setCache(Location block, Shop shop) {
        accessCaching.put(block, shop);
        cachingExpire.put(block, System.currentTimeMillis());
    }

    public void resetCache(Location block) {
        accessCaching.remove(block);
        cachingExpire.remove(block);
    }

}
