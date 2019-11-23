package org.maxgamer.quickshop.Event;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop.Shop;

/**
 * Calling when shop price was changed, Can't cancel
 **/

public class ShopPriceChangeEvent extends QSEvent implements Cancellable {

    @Getter
    private final double newPrice;

    @Getter
    private final double oldPrice;

    @Getter
    @NotNull
    private final Shop shop;

    private boolean cancelled;

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

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
