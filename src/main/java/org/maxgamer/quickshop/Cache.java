package org.maxgamer.quickshop;

import com.google.common.cache.CacheBuilder;
import org.bukkit.Location;
import org.maxgamer.quickshop.shop.Shop;

import java.util.concurrent.TimeUnit;

public class Cache {
    private final QuickShop plugin;
    private final com.google.common.cache.Cache<Location, CacheContainer> accessCaching = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).initialCapacity(500).build();

    public Cache(QuickShop plugin) {
        this.plugin = plugin;
    }

    public long getCachingSize() {
        return accessCaching.size();
    }

    public Shop getCaching(Location location, boolean includeAttached) {
        CacheContainer container;
            container = accessCaching.getIfPresent(location);
        if (container == null) {
            if (includeAttached) {
                return plugin.getShopManager().getShopIncludeAttached(location);
            } else {
                return plugin.getShopManager().getShop(location);
            }
        }else{
            return container.getShop();
        }
    }


    public void setCache(Location location, Shop shop) {
        accessCaching.put(location, new CacheContainer(shop, System.currentTimeMillis()));
    }
}

class CacheContainer {
    private Shop shop;

    private long time;

    public CacheContainer(Shop shop, long time) {
        this.shop = shop;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(final Shop shop) {
        this.shop = shop;
    }

}

