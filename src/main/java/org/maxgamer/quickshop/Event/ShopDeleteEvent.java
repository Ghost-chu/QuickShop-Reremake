package org.maxgamer.quickshop.Event;

import lombok.Getter;
import lombok.ToString;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop.Shop;
@ToString
public class ShopDeleteEvent extends QSEvent implements Cancellable {

    @Getter
    private final boolean fromMemory;

    @Getter
    @NotNull
    private final Shop shop;

    private boolean cancelled;

    /**
     * Call the event when shop is deleteing.
     * The ShopUnloadEvent will call after ShopDeleteEvent
     *
     * @param shop       Target shop
     * @param fromMemory Only delete from the memory? false = delete both in memory and database
     */
    public ShopDeleteEvent(@NotNull Shop shop, boolean fromMemory) {
        this.shop = shop;
        this.fromMemory = fromMemory;
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
