package org.maxgamer.quickshop;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.maxgamer.quickshop.shop.Shop;

public class Cache extends TimerTask {
    private QuickShop plugin;

    private long expireTime = 5000;

    private Map<Location, CacheContainer> accessCaching = new HashMap<>(1000);

    private final Object lock = new Object();

    public Cache(QuickShop plugin) {
        this.plugin = plugin;
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanCache();
            }
        }.runTaskTimerAsynchronously(plugin, 0, expireTime * 20);
    }

    public int getCachingSize() {
        return accessCaching.size();
    }

    public Shop getCaching(Location location, boolean includeAttached) {
        CacheContainer container;
        synchronized (lock) {
            container = accessCaching.get(location);
        }
        if (container == null) {
            return null;
        }
        if (isExpired(container)) {
            if (includeAttached) {
                return plugin.getShopManager().getShopIncludeAttached(location);
            } else {
                return plugin.getShopManager().getShop(location);
            }
        }
        return container.getShop();
    }

    public boolean isExpired(CacheContainer container) {
        return System.currentTimeMillis() - container.getTime() > expireTime;
    }

    public void setCache(Location location, Shop shop) {
        synchronized (lock) {
            accessCaching.put(location, new CacheContainer(shop, System.currentTimeMillis()));
        }
    }

    public void cleanCache() {
        synchronized (lock) {
            accessCaching.keySet().removeIf(e -> isExpired(accessCaching.get(e)));
        }
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        cleanCache();
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

    public Shop getShop() {
        return shop;
    }

    public void setShop(final Shop shop) {
        this.shop = shop;
    }

    public void setTime(long time) {
        this.time = time;
    }

}

