package org.maxgamer.quickshop.Event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called before the shop creation request is sent. E.g. A player
 * clicks a chest, this event is thrown, if successful, the player is asked how
 * much they wish to trade for.
 */
public class ShopPreCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Player p;
    private Location loc;

    public ShopPreCreateEvent(Player p, Location loc) {
        this.loc = loc;
        this.p = p;
    }

    /**
     * The location of the shop that will be created.
     *
     * @return The location of the shop that will be created.
     */
    public Location getLocation() {
        return loc;
    }

    /**
     * The player who is creating this shop
     *
     * @return The player who is creating this shop
     */
    public Player getPlayer() {
        return p;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
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