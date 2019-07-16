package org.maxgamer.quickshop.Event;

import lombok.*;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Shop.Shop;

/** Calling when shop price was changed, Can't cancel **/
@Builder
public class ShopPriceChangedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    @NotNull
    private Shop shop;
    @Getter private double oldPrice;
    @Getter private double newPrice;

    /**
     * Will call when shop price was changed.
     *
     * @param shop     Target shop
     * @param oldPrice The old shop price
     * @param newPrice The new shop price
     */
    public ShopPriceChangedEvent(@NotNull Shop shop, double oldPrice, double newPrice) {
        this.shop = shop;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
