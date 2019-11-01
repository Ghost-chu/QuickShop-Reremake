package org.maxgamer.quickshop.Event;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop.Shop;

/**
 * Calling when shop price was changed, Can't cancel
 **/

public class ShopPriceChangeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    @Getter
    private double newPrice;
    @Getter
    private double oldPrice;
    @Getter
    @NotNull
    private Shop shop;
    private boolean isCancelled;

    /**
     * Will call when shop price was changed.
     *
     * @param shop     Target shop
     * @param oldPrice The old shop price
     * @param newPrice The new shop price
     */
    public ShopPriceChangeEvent(@NotNull Shop shop, double oldPrice, double newPrice) {
        this.shop = shop;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.isCancelled = b;
    }
}
