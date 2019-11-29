package org.maxgamer.quickshop.Event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ShopInventoryPreviewEvent extends QSEvent implements Cancellable {

    @Getter
    @NotNull
    private final ItemStack itemStack;

    @Getter
    @NotNull
    private final Player player;

    private boolean cancelled;

    /**
     * Build a event when player using GUI preview
     *
     * @param player    Target plugin
     * @param itemStack The preview item, with preview flag.
     */
    public ShopInventoryPreviewEvent(@NotNull Player player, @NotNull ItemStack itemStack) {
        this.player = player;
        this.itemStack = itemStack;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
