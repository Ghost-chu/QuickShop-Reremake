package org.maxgamer.quickshop.Event;

import lombok.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Shop.Shop;

public class ShopDeleteEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    @NotNull
    private Shop shop;
    private boolean cancelled;
    @Getter private boolean fromMemory;

    /**
     * Call the event when shop is deleteing.
     * The ShopUnloadEvent will call after ShopDeleteEvent
     *
     * @param shop       Target shop
     * @param fromMemory Only delete from the memory? false = delete both in memory and database
     */
    public ShopDeleteEvent(@NotNull Shop shop, boolean fromMemory) {
        this.shop = shop;
        this.fromMemory = fromMemory;
    }

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
