package org.maxgamer.quickshop.Event;

import lombok.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Shop.Shop;

public class ShopSuccessPurchaseEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    @Getter private int amount;
    private boolean cancelled;
    @Getter
    @NotNull
    private Player player;
    @Getter
    @NotNull
    private Shop shop;
    @Getter private double tax;
    private double total;

    /**
     * Builds a new shop purchase event
     * This time, purchase not start, please listen the ShopSuccessPurchaseEvent.
     *
     * @param shop   The shop bought from
     * @param p      The player buying
     * @param amount The amount they're buying
     * @param tax The tax in this purchase
     * @param total The money in this purchase
     */
    public ShopSuccessPurchaseEvent(@NotNull Shop shop, @NotNull Player p, int amount, double total, double tax) {
        this.shop = shop;
        this.player = p;
        this.amount = amount;
        this.tax = tax;
        this.total = total;
    }


    /**
     * The total money changes in this purchase.
     * Calculate tax, if you want get total without tax, please use getBalanceWithoutTax()
     *
     * @return the total money with calculate tax
     */
    public double getBalance() {return this.total * (1 - tax);}

    /**
     * The total money changes in this purchase.
     * No calculate tax, if you want get total with tax, please use getBalance()
     *
     * @return the total money without calculate tax
     */
    public double getBalanceWithoutTax() {return this.total;}

    public static HandlerList getHandlerList() {
        return handlers;
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