package org.maxgamer.quickshop;

import com.google.common.cache.CacheBuilder;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    @Nullable
    public Shop getCaching(@NotNull Location location, boolean includeAttached) {
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

    public void setCache(@NotNull Location location, @Nullable Shop shop) {
        accessCaching.put(location, new CacheContainer(shop, System.currentTimeMillis()));
    }
}

class CacheContainer {
    private final  Shop shop;

    private final long time;

    public CacheContainer(Shop shop, long time) {
        this.shop = shop;
        this.time = time;
    }

    public long getTime() {
        return time;
    }
    public Shop getShop() {
        return shop;
    }


}

