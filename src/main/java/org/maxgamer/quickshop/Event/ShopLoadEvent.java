package org.maxgamer.quickshop.Event;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop.Shop;

/**
 * Call when loading shop
 **/

public class ShopLoadEvent extends QSEvent implements Cancellable {

    @Getter
    @NotNull
    private final Shop shop;

    private boolean cancelled;

    /**
     * Calling when shop loading
     *
     * @param shop Target shop
     */
    public ShopLoadEvent(@NotNull Shop shop) {
        this.shop = shop;
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
