package org.maxgamer.quickshop.Event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Shop.Shop;

/** Call when loading shop **/
@SuppressWarnings("WeakerAccess")
public class ShopLoadEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Shop shop;


    public ShopLoadEvent(Shop shop) {
        this.shop = shop;
    }

    public Shop getShop() {
        return shop;
    }

    @NotNull
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
