package org.maxgamer.quickshop.Shop;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShopCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private Shop shop;
    private boolean cancelled;
    private Player p;

    public ShopCreateEvent(Shop shop, Player p) {
        this.shop = shop;
        this.p = p;
    }

    /**
     * The shop to be created
     *
     * @return The shop to be created
     */
    public Shop getShop() {
        return this.shop;
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