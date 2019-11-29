package org.maxgamer.quickshop.Event;

import lombok.Getter;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop.Shop;

public class ShopUpdateEvent extends QSEvent implements Cancellable {

    @NotNull
    @Getter
    private final Shop shop;

    private boolean cancelled;

    /**
     * Call when shop is trying updated to database
     *
     * @param shop The shop bought from
     */
    public ShopUpdateEvent(@NotNull Shop shop) {
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
