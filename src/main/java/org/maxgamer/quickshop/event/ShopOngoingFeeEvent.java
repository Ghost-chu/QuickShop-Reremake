package org.maxgamer.quickshop.event;

import org.bukkit.event.Cancellable;
import org.maxgamer.quickshop.api.shop.Shop;

import java.util.UUID;

public class ShopOngoingFeeEvent extends AbstractQSEvent implements Cancellable {
    private final UUID player;

    private final Shop shop;

    private double cost;

    public ShopOngoingFeeEvent(Shop shop, UUID player, double cost) {
        this.shop = shop;
        this.player = player;
        this.cost = cost;
    }

    /**
     * Sets the ongoing fee to replace old one
     *
     * @param cost The ongoing fee
     */
    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     * Getting the cost in this event
     *
     * @return The ongoing fee
     */
    public double getCost() {
        return cost;
    }

    /**
     * Getting related shop in this event
     *
     * @return The shop triggered ongoing fee event
     */
    public Shop getShop() {
        return shop;
    }

    /**
     * Getting related player in this event
     *
     * @return The player triggered ongoing fee event
     */
    public UUID getPlayer() {
        return player;
    }

    private boolean cancelled;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
