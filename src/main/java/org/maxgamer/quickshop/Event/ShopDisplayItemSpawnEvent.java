package org.maxgamer.quickshop.Event;

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
public class ShopDisplayItemSpawnEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Shop shop;
    private ItemStack iStack;
    private DisplayType displayType;

    /**
     * This event is called before the shop display item created
     * @param shop Target shop
     * @param displayType The displayType
     * @param iStack Target ItemStack
     */
    public ShopDisplayItemSpawnEvent(Shop shop, ItemStack iStack, DisplayType displayType) {
        this.shop = shop;
        this.iStack = iStack;
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
        this.iStack = iStack;
        this.displayType = DisplayItem.getNowUsing();
    }

    public Shop getShop() {
        return shop;
    }

    public DisplayType getDisplayType() {
        return displayType;
    }

    public ItemStack getItemStack() {
        return iStack;
    }

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
