package org.maxgamer.quickshop.Event;

import lombok.Getter;
import lombok.ToString;
import org.bukkit.event.Cancellable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop.DisplayItem;
import org.maxgamer.quickshop.Shop.DisplayType;
import org.maxgamer.quickshop.Shop.Shop;

/**
 * This event is called after DisplayItem removed
 */
@ToString
public class ShopDisplayItemDespawnEvent extends QSEvent implements Cancellable {

    @Getter
    @NotNull
    private final Shop shop;

    @Getter
    @NotNull
    private final ItemStack itemStack;

    @Getter
    @NotNull
    private final DisplayType displayType;

    private boolean cancelled;

    /**
     * This event is called before the shop display item created
     *
     * @param shop        Target shop
     * @param itemStack      Target itemstacck
     * @param displayType The displayType
     */
    public ShopDisplayItemDespawnEvent(@NotNull Shop shop, @NotNull ItemStack itemStack,
                                       @NotNull DisplayType displayType) {
        this.shop = shop;
        this.itemStack = itemStack;
        this.displayType = displayType;
    }

    /**
     * This event is called before the shop display item created
     *
     * @param shop      Target shop
     * @param itemStack Target itemstacck
     */
    @Deprecated
    public ShopDisplayItemDespawnEvent(@NotNull Shop shop, @NotNull ItemStack itemStack) {
        this(shop, itemStack, DisplayItem.getNowUsing());
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
