package org.maxgamer.quickshop.Event;

import lombok.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.*;

/**
 * This event is called before the shop creation request is sent. E.g. A player
 * clicks a chest, this event is thrown, if successful, the player is asked how
 * much they wish to trade for.
 */
@Builder
public class ShopPreCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    @Getter
    @NotNull
    private Player player;
    @Getter
    @NotNull
    private Location location;

    /**
     * Calling when shop pre-creating.
     * Shop won't one-percent will create after this event, if you want get the shop created event, please use ShopCreateEvent
     * @param p Target player
     * @param loc The location will create be shop
     */
    public ShopPreCreateEvent(@NotNull Player p, @NotNull Location loc) {
        this.location = loc;
        this.player = p;
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