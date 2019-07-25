package org.maxgamer.quickshop.Event;

import lombok.*;
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
    @Getter
    @NotNull
    private Shop shop;

    /**
     * Calling when shop loading
     *
     * @param shop Target shop
     */
    public ShopLoadEvent(@NotNull Shop shop) {
        this.shop = shop;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public HandlerList getHandlerList() {return handlers;}
    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
