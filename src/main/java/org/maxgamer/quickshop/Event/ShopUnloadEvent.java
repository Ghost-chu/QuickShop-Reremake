package org.maxgamer.quickshop.Event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.maxgamer.quickshop.Shop.Shop;

/** Getting the unloading shop, Can't cancel. **/
public class ShopUnloadEvent extends Event {
    /* Getting the unloading shop, Can't cancel. **/
    public ShopUnloadEvent(Shop shop) {
        this.shop = shop;
    }

    private static final HandlerList handlers = new HandlerList();
    private Shop shop;

    public Shop getShop() {
        return shop;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
