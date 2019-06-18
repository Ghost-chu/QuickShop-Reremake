package org.maxgamer.quickshop.Event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Shop.Shop;

public class ShopDeleteEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Shop shop;
    private boolean cancelled;
    private boolean fromMemory;

    /**
     * Call the event when shop is deleteing.
     * The ShopUnloadEvent will call after ShopDeleteEvent
     *
     * @param shop       Target shop
     * @param fromMemory Only delete from the memory? false = delete both in memory and database
     */
    public ShopDeleteEvent(Shop shop, boolean fromMemory) {
        this.shop = shop;
        this.fromMemory = fromMemory;
    }

    /**
     * Get is only delete from the memory
     *
     * @return only from memory
     */
    public boolean isFromMemory() {
        return this.fromMemory;
    }

    /**
     * The shop to be deleted
     *
     * @return The shop to be deleted
     */
    public Shop getShop() {
        return this.shop;
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
