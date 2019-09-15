package org.maxgamer.quickshop.Event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShopInventoryPreviewEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    @Getter
    @NotNull
    private ItemStack itemStack;
    @Getter
    @NotNull
    private Player player;

    /**
     * Build a event when player using GUI preview
     *
     * @param p         Target plugin
     * @param itemStack The preview item, with preview flag.
     */
    public ShopInventoryPreviewEvent(@NotNull Player p, @NotNull ItemStack itemStack) {
        this.player = p;
        this.itemStack = itemStack;
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
