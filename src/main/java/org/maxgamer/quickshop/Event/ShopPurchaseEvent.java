package org.maxgamer.quickshop.Event;

import lombok.Getter;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop.Shop;
@ToString
public class ShopPurchaseEvent extends QSEvent implements Cancellable {

    @Getter
    @NotNull
    private final Shop shop;

    @Getter
    @NotNull
    private final Player player;

    @Getter
    private final int amount;

    private boolean cancelled;

    /**
     * Builds a new shop purchase event
     * This time, purchase not start, please listen the ShopSuccessPurchaseEvent.
     *
     * @param shop   The shop bought from
     * @param player The player buying
     * @param amount The amount they're buying
     */
    public ShopPurchaseEvent(@NotNull Shop shop, @NotNull Player player, int amount) {
        this.shop = shop;
        this.player = player;
        this.amount = amount;
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
