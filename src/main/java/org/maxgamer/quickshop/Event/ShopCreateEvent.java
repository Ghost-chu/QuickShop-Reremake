package org.maxgamer.quickshop.Event;

import lombok.Getter;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop.Shop;

@ToString
public class ShopCreateEvent extends QSEvent implements Cancellable {

    @Getter
    @NotNull
    private final Player player;

    @Getter
    @NotNull
    private final Shop shop;

    private boolean cancelled;

    /**
     * Call when have a new shop was createing.
     *
     * @param shop   Target shop
     * @param player The player creaing the shop
     */
    public ShopCreateEvent(@NotNull Shop shop, @NotNull Player player) {
        this.shop = shop;
        this.player = player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
