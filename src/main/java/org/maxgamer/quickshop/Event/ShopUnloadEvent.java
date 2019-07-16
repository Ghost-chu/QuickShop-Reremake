package org.maxgamer.quickshop.Event;

import lombok.*;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Shop.Shop;

/** Getting the unloading shop, Can't cancel. **/
@Builder
public class ShopUnloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    private @NotNull Shop shop;
    /* Getting the unloading shop, Can't cancel. **/
    public ShopUnloadEvent(@NotNull Shop shop) {
        this.shop = shop;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
