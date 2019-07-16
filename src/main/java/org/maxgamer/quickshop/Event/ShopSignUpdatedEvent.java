package org.maxgamer.quickshop.Event;

import lombok.*;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Shop.Shop;

/** Calling when shop sign update, Can't cancel **/
@Builder
public class ShopSignUpdatedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    @NonNull
    private Shop shop;
    @Getter
    @NonNull
    private Sign sign;

    /**
     * Will call when shop price was changed.
     *
     * @param shop Target shop
     * @param sign updated sign
     */
    public ShopSignUpdatedEvent(@NonNull Shop shop, @NonNull Sign sign) {
        this.shop = shop;
        this.sign = sign;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
