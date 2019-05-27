package org.maxgamer.quickshop.Shop;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class ShopDisplayItemSpawnEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Shop shop;
    private ItemStack iStack;
    private boolean fakeItem;

    /**
     * This event is called before the shop display item created
     */
    public ShopDisplayItemSpawnEvent(Shop shop, ItemStack iStack) {
        this.shop = shop;
        this.iStack = iStack;
    }

    /**
     * This event is called before the shop display item created
     */
    public ShopDisplayItemSpawnEvent(Shop shop, ItemStack iStack, boolean fakeItem) {
        this.shop = shop;
        this.iStack = iStack;
        this.fakeItem = fakeItem;
    }

    public Shop getShop() {
        return shop;
    }

    public ItemStack getItemStack() {
        return iStack;
    }

    public boolean getFakeItem() {return fakeItem;}

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
