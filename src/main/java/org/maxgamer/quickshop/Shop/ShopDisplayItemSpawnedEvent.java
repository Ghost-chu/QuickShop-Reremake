package org.maxgamer.quickshop.Shop;

import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is called after the shop display item created
 */
public class ShopDisplayItemSpawnedEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Shop shop;
    private Item item;
    private boolean fakeItem;

    /*
     * This event is called after the shop display item created
     */
    public ShopDisplayItemSpawnedEvent(Shop shop, Item item) {
        this.shop = shop;
        this.item = item;
    }

    /*
     * This event is called after the shop display item created
     */
    public ShopDisplayItemSpawnedEvent(Shop shop, Item item, boolean fakeItem) {
        this.shop = shop;
        this.item = item;
        this.fakeItem = fakeItem;
    }

    public Shop getShop() {
        return shop;
    }

    public Item getItem() {
        return item;
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
