package org.maxgamer.quickshop.Util;

import java.util.WeakHashMap;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.Event.ShopCreateEvent;
import org.maxgamer.quickshop.Event.ShopDeleteEvent;
import org.maxgamer.quickshop.Shop.Shop;

public class CachingPool implements Listener {
    public WeakHashMap<Location, Shop> shopCaching = new WeakHashMap<>(100);

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void shopUpdateEvent(ShopCreateEvent shopCreateEvent) {
        shopCaching.put(shopCreateEvent.getShop().getLocation(), shopCreateEvent.getShop());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void shopDeleteEvent(ShopDeleteEvent shopDeleteEvent) {
        shopCaching.put(shopDeleteEvent.getShop().getLocation(), null);
    }

}
