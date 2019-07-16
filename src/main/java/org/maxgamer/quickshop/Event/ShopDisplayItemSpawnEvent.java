package org.maxgamer.quickshop.Event;

import lombok.*;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.Shop.DisplayItem;
import org.maxgamer.quickshop.Shop.DisplayType;
import org.maxgamer.quickshop.Shop.Shop;

/**
 * This event is called before the shop display item created
 */
@Builder
public class ShopDisplayItemSpawnEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    @Getter
    @NonNull
    private Shop shop;
    @Getter
    @NonNull
    private ItemStack itemStack;
    @Getter
    @NonNull
    private DisplayType displayType;

    /**
     * This event is called before the shop display item created
     * @param shop Target shop
     * @param displayType The displayType
     * @param iStack Target ItemStack
     */
    public ShopDisplayItemSpawnEvent(@NonNull Shop shop, @NonNull ItemStack iStack, @NonNull DisplayType displayType) {
        this.shop = shop;
        this.itemStack = iStack;
        this.displayType = displayType;
    }

    /**
     * This event is called before the shop display item created
     * @param shop Target shop
     * @param displayType The displayType
     */
    @Deprecated
    public ShopDisplayItemSpawnEvent(Shop shop, ItemStack iStack) {
        this.shop = shop;
        this.itemStack = iStack;
        this.displayType = DisplayItem.getNowUsing();
    }

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
