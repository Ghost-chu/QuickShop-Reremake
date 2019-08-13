package org.maxgamer.quickshop.Event;

import lombok.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Shop.Shop;

public class ShopClickEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    @Getter
    private @NotNull Shop shop;

    /**
     * Call when shop was clicked.
     *
     * @param shop The shop bought from
     */
    public ShopClickEvent(@NotNull Shop shop) {
        this.shop = shop;
    }

    public static HandlerList getHandlerList() {return handlers;}

    @NotNull
    @Override
    public HandlerList getHandlers() {
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