package org.maxgamer.quickshop.Event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.*;

public class ShopInventoryPreviewEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Player p;
    private ItemStack itemStack;

    /**
     * Build a event when player using GUI preview
     *
     * @param p         Target plugin
     * @param itemStack The preview item, with preview flag.
     */
    public ShopInventoryPreviewEvent(Player p, ItemStack itemStack) {
        this.p = p;
        this.itemStack = itemStack;
    }

    /**
     * Get the preview ItemStack
     *
     * @return Itemstack for previewing
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * The player who is preview item
     *
     * @return The player who is previewing
     */
    public Player getPlayer() {
        return p;
    }

    @NotNull
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
