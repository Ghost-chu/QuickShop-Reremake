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


    /**
     * Gets shop from plugin caching
     *
     * @param location        The shop location that you want to get
     * @param includeAttached Include attached shops
     * @return The shop, null for no shops found in caching and memory
     */
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
        } else {
            return container.getShop();
        }
    }

    /**
     * Update and invalidate the caching
     *
     * @param location The location that you want to update
     * @param shop     null for invalidate and Shop object for update
     */
    public void setCache(@NotNull Location location, @Nullable Shop shop) {
        if (shop == null) {
            accessCaching.invalidate(location);
            return;
        }
        accessCaching.put(location, new CacheContainer(shop, System.currentTimeMillis()));
    }
}

class CacheContainer {
    @NotNull
    private final Shop shop;

    private final long time;

    public CacheContainer(@NotNull Shop shop, long time) {
        this.shop = shop;
        this.time = time;
    }

    /**
     * Gets container created at.
     *
     * @return The timestamp
     */
    public long getTime() {
        return time;
    }

    /**
     * Gets container shop
     *
     * @return The shop
     */
    @NotNull
    public Shop getShop() {
        return shop;
    }
}

