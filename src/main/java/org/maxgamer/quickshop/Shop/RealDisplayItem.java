package org.maxgamer.quickshop.Shop;

import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Event.ShopDisplayItemDespawnEvent;
import org.maxgamer.quickshop.Event.ShopDisplayItemSpawnEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

@ToString
public class RealDisplayItem implements DisplayItem {

    private static QuickShop plugin = QuickShop.instance;
    private ItemStack guardedIstack;
    private Item item;
    private ItemStack originalItemStack;
    private Shop shop;

    /**
     * ZZ Creates a new display item.
     *
     * @param shop The shop (See Shop)
     */
    RealDisplayItem(@NotNull Shop shop) {
        this.shop = shop;
        this.originalItemStack = shop.getItem().clone();
        this.originalItemStack.setAmount(1);

        // this.displayLoc = shop.getLocation().clone().add(0.5, 1.2, 0.5);
    }

    @Override
    public boolean checkDisplayIsMoved() {
        if (this.item == null) {
            return false;
        }
        //return !this.item.getLocation().equals(getDisplayLocation());
        /* We give 0.6 block to allow item drop on the chest, not floating on the air. */
        if (!this.item.getLocation().getWorld().equals(getDisplayLocation().getWorld())) {
            return true;
        }
        return this.item.getLocation().distance(getDisplayLocation()) > 0.6;
    }

    @Override
    public boolean checkDisplayNeedRegen() {
        if (this.item == null) {
            return false;
        }
        return !this.item.isValid() || this.item.isDead();
    }

    @Override
    public boolean checkIsShopEntity(@NotNull Entity entity) {
        if (!(entity instanceof Item)) {
            return false;
        }
        return DisplayItem.checkIsGuardItemStack(((Item) entity).getItemStack());
    }

    @Override
    public void fixDisplayMoved() {
        for (Entity entity : this.shop.getLocation().getWorld().getEntities()) {
            if (!(entity instanceof Item)) {
                continue;
            }
            Item eItem = (Item) entity;
            if (eItem.getUniqueId().equals(this.item.getUniqueId())) {
                Util.debugLog("Fixing moved Item displayItem " + eItem.getUniqueId().toString() + " at " + eItem
                        .getLocation().toString());
                eItem.teleport(getDisplayLocation());
                return;
            }
        }
    }

    @Override
    public void fixDisplayNeedRegen() {
        respawn();
    }

    @Override
    public void remove() {
        if (this.item == null || !this.item.isValid() || this.item.isDead()) {
            Util.debugLog("Ignore the Item removing because the Item is already gone.");
            return;
        }
        this.item.remove();
        this.item = null;
        this.guardedIstack = null;
        ShopDisplayItemDespawnEvent shopDisplayItemDepawnEvent = new ShopDisplayItemDespawnEvent(shop, originalItemStack, DisplayType.REALITEM);
        Bukkit.getPluginManager().callEvent(shopDisplayItemDepawnEvent);
    }

    @Override
    public boolean removeDupe() {
        if (this.item == null) {
            Util.debugLog("Warning: Trying to removeDupe for a null display shop.");
            return false;
        }
        boolean removed = false;
        //Chunk chunk = shop.getLocation().getChunk();
        for (Entity entity : item.getNearbyEntities(1, 1, 1)) {
            if (!(entity instanceof Item)) {
                continue;
            }
            Item eItem = (Item) entity;
            if (!DisplayItem.checkIsGuardItemStack(eItem.getItemStack())) {
                Util.debugLog(Util
                        .getItemStackName(eItem.getItemStack()) + " not a shop displayItem: Failed check guardedItemStack");
                continue;
            }
            if (!eItem.getUniqueId().equals(this.item.getUniqueId())) {
                if (DisplayItem.checkIsTargetShopDisplay(eItem.getItemStack(), this.shop)) {
                    Util.debugLog("Removing a duped ItemEntity " + eItem.getUniqueId().toString() + " at " + eItem
                            .getLocation().toString());
                    entity.remove();
                    removed = true;
                } else {
                    Util.debugLog(Util
                            .getItemStackName(eItem.getItemStack()) + " not a shop displayItem: Failed check shop display.");
                }
            }
        }
        return removed;
    }

    @Override
    public void respawn() {
        remove();
        spawn();
    }

    @Override
    public void safeGuard(@NotNull Entity entity) {
        if (!(entity instanceof Item)) {
            Util.debugLog("Failed to safeGuard " + entity.getLocation().toString() + ", cause target not a Item");
            return;
        }
        Item item = (Item) entity;
        //Set item protect in the armorstand's hand
        this.guardedIstack = DisplayItem.createGuardItemStack(this.originalItemStack, this.shop);
        item.setItemStack(this.guardedIstack);
        Util.debugLog("Successfully safeGuard Item: " + item.getLocation().toString());
    }

    @Override
    public void spawn() {
        if (shop.getLocation().getWorld() == null) {
            Util.debugLog("Canceled the displayItem spawning because the location in the world is null.");
            return;
        }

        if (originalItemStack == null) {
            Util.debugLog("Canceled the displayItem spawning because the ItemStack is null.");
            return;
        }
        if (item != null && item.isValid() && !item.isDead()) {
            Util.debugLog("Warning: Spawning the Dropped Item for DisplayItem when there is already an existing Dropped Item, May cause a duplicated Dropped Item!");
            StackTraceElement[] traces = Thread.currentThread().getStackTrace();
            for (StackTraceElement trace : traces) {
                Util.debugLog(trace.getClassName() + "#" + trace.getMethodName() + "#" + trace.getLineNumber());
            }
        }
        if (!Util.isDisplayAllowBlock(getDisplayLocation().getBlock().getType())) {
            Util.debugLog("Can't spawn the displayItem because there is not an AIR block above the shopblock.");
            return;
        }

        ShopDisplayItemSpawnEvent shopDisplayItemSpawnEvent = new ShopDisplayItemSpawnEvent(shop, originalItemStack, DisplayType.REALITEM);
        Bukkit.getPluginManager().callEvent(shopDisplayItemSpawnEvent);
        if (shopDisplayItemSpawnEvent.isCancelled()) {
            Util.debugLog("Canceled the displayItem spawning because a plugin setCancelled the spawning event, usually this is a QuickShop Add on");
            return;
        }

        this.item = this.shop.getLocation().getWorld().dropItem(getDisplayLocation(), originalItemStack);
        this.item.setPickupDelay(Integer.MAX_VALUE);
        this.item.setSilent(true);
        this.item.setPortalCooldown(Integer.MAX_VALUE);
        this.item.setVelocity(new Vector(0, 0.1, 0));
        this.item.setCustomNameVisible(false);
        safeGuard(this.item);
        Util.debugLog("Spawned new DisplayItem for shop " + shop.getLocation().toString());
    }

    @Override
    public @Nullable Entity getDisplay() {
        return this.item;
    }

    @Override
    public @Nullable Location getDisplayLocation() {
        return this.shop.getLocation().clone().add(0.5, 1.2, 0.5);
    }

    @Override
    public boolean isSpawned() {
        if (this.item == null) {
            return false;
        }
        return this.item.isValid();
    }
}
