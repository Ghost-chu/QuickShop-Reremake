package org.maxgamer.quickshop.Event;

import lombok.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Shop.Shop;

@Builder
public class ShopCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Getter
    @NonNull
    private Shop shop;
    private boolean cancelled;
    @Getter
    @NonNull
    private Player player;

    /**
     * Call when have a new shop was createing.
     *
     * @param shop Target shop
     * @param p    The player creaing the shop
     */
    public ShopCreateEvent(@NonNull Shop shop, @NonNull Player p) {
        this.shop = shop;
        this.player = p;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}