package org.maxgamer.quickshop.Event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.maxgamer.quickshop.Shop.Shop;

public class ShopUpdateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Shop shop;
    private boolean cancelled;

    /**
     * Call when shop is trying updated to database
     *
     * @param shop The shop bought from
     */
    public ShopUpdateEvent(Shop shop) {
        this.shop = shop;
    }

    /**
     * The shop used in this event
     *
     * @return The shop used in this event
     */
    public Shop getShop() {
        return this.shop;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
