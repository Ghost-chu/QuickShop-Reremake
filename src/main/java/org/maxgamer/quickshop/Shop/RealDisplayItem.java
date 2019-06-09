package org.maxgamer.quickshop.Shop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Event.ShopDisplayItemSpawnEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

public class RealDisplayItem implements DisplayItem {

    private Shop shop;
    private ItemStack originalItemStack;
    private ItemStack guardedIstack;
    private Item item;
    static QuickShop plugin = QuickShop.instance;

    /**
     * ZZ Creates a new display item.
     *
     * @param shop The shop (See Shop)
     */
    public RealDisplayItem(@NotNull Shop shop) {
        this.shop = shop;
        this.originalItemStack = shop.getItem().clone();

        // this.displayLoc = shop.getLocation().clone().add(0.5, 1.2, 0.5);
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
        if (!Util.isAir(getDisplayLocation().add(0, 1, 0).getBlock().getType())) {
            Util.debugLog("Can't spawn the displayItem because there is not an AIR block above the shopblock.");
            return;
        }

        ShopDisplayItemSpawnEvent shopDisplayItemSpawnEvent = new ShopDisplayItemSpawnEvent(shop, originalItemStack);
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
        safeGuard(this.item);
        Util.debugLog("Spawned new DisplayItem for shop " + shop.getLocation().toString());
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
    public void remove() {
        if (this.item == null || !this.item.isValid() || this.item.isDead()) {
            Util.debugLog("Ignore the Item removing because the Item is already gone.");
            return;
        }
        this.item.remove();
        this.item = null;
        this.guardedIstack = null;
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
            if (!(entity instanceof Item))
                continue;
            Item eItem = (Item) entity;
            if (!DisplayItem.checkIsGuardItemStack(eItem.getItemStack()))
                continue;
            if (plugin.getItemMatcher().matches(eItem.getItemStack(), this.guardedIstack)) {
                if (!eItem.getUniqueId().equals(this.item.getUniqueId())) {
                    if (DisplayItem.checkIsTargetShopDisplay(eItem.getItemStack(), this.shop)) {
                        Util.debugLog("Removing a duped ItemEntity " + eItem.getUniqueId().toString() + " at " + eItem
                                .getLocation().toString());
                        entity.remove();
                        removed = true;
                    }
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
    public Entity getDisplay() {
        return this.item;
    }

    @Override
    public boolean checkIsShopEntity(@NotNull Entity entity) {
        if (!(entity instanceof Item))
            return false;
        return DisplayItem.checkIsGuardItemStack(((Item) entity).getItemStack());
    }

    @Override
    public boolean checkDisplayNeedRegen() {
        if (this.item == null)
            return false;
        return !this.item.isValid() || this.item.isDead();
    }

    @Override
    public boolean checkDisplayIsMoved() {
        if (this.item == null)
            return false;
        //return !this.item.getLocation().equals(getDisplayLocation());
        /* We give 0.6 block to allow item drop on the chest, not floating on the air. */
        return this.item.getLocation().distance(getDisplayLocation()) > 0.6;
    }

    @Override
    public void fixDisplayNeedRegen() {
        respawn();
    }

    @Override
    public void fixDisplayMoved() {
        for (Entity entity : this.shop.getLocation().getWorld().getEntities()) {
            if (!(entity instanceof Item))
                continue;
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
    public boolean isSpawned() {
        if (this.item == null)
            return false;
        return this.item.isValid();
    }

    // /**
    //  * Spawns the dummy item on top of the shop.
    //  */
    // public void spawn() {
    //     if (shop.getLocation().getWorld() == null) {
    //         return;
    //     }
    //     Location dispLoc = this.getDisplayLocation();
    //     //Call Event for QSAPI
    //
    //     ShopDisplayItemSpawnEvent shopDisplayItemSpawnEvent = new ShopDisplayItemSpawnEvent(shop, iStack);
    //     Bukkit.getPluginManager().callEvent(shopDisplayItemSpawnEvent);
    //     if (shopDisplayItemSpawnEvent.isCancelled()) {
    //         return;
    //     }
    //     this.item = shop.getLocation().getWorld().dropItem(dispLoc, this.iStack);
    //     this.item.setVelocity(new Vector(0, 0.1, 0));
    //     try {
    //         this.safeGuard(this.item);
    //         ShopDisplayItemSpawnedEvent shopDisplayItemSpawnedEvent = new ShopDisplayItemSpawnedEvent(shop, this.item);
    //         Bukkit.getPluginManager().callEvent(shopDisplayItemSpawnedEvent);
    //         // NMS.safeGuard
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         plugin.getLogger().log(Level.WARNING,
    //                 "QuickShop version mismatch! This version of QuickShop is incompatible with this version of bukkit! Try update?");
    //     }
    // }
    //
    // /**
    //  * Spawns the new display item. Does not remove duplicate items.
    //  */
    // public void respawn() {
    //     if (item.isValid() == false) {
    //         spawn();
    //     }
    // }
    //
    // /**
    //  * Set item is QuickShop's DisplayItem and prevent them.
    //  *
    //  * @param entity
    //  */
    // public void safeGuard(Entity entity) {
    //     Item item = (Item) entity;
    //     item.setPickupDelay(Integer.MAX_VALUE);
    //     ItemMeta iMeta = item.getItemStack().getItemMeta();
    //
    //     if (plugin.getConfig().getBoolean("shop.display-item-use-name")) {
    //         item.setCustomName("QuickShop");
    //         iMeta.setDisplayName("QuickShop");
    //     }
    //     item.setPortalCooldown(Integer.MAX_VALUE);
    //     item.setSilent(true);
    //     item.setInvulnerable(true);
    //     java.util.List<String> lore = new ArrayList<String>();
    //     for (int i = 0; i < 21; i++) {
    //         lore.add("QuickShop DisplayItem"); //Create 20 lines lore to make sure no stupid plugin accident remove mark.
    //     }
    //     iMeta.setLore(lore);
    //     item.getItemStack().setItemMeta(iMeta);
    // }
    //
    // /**
    //  * Check the ItemStack is or not a DisplayItem
    //  *
    //  * @param itemStack The ItemStack you want to check.
    //  * @return The check result.
    //  */
    // public boolean checkIsShopEntity(ItemStack itemStack) {
    //     if (itemStack == null) {
    //         return false;
    //     }
    //     if (!itemStack.hasItemMeta()) {
    //         return false;
    //     }
    //     if (itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().contains("QuickShop")) {
    //         return true;
    //     }
    //     if (itemStack.getItemMeta().hasLore()) {
    //         List<String> lores = itemStack.getItemMeta().getLore();
    //         for (String singleLore : lores) {
    //             if (singleLore.equals("QuickShop DisplayItem") || singleLore.contains("QuickShop DisplayItem")) {
    //                 return true;
    //             }
    //         }
    //     }
    //     return false;
    // }
    //
    // /**
    //  * Removes all items floating ontop of the chest that aren't the display
    //  * item.
    //  */
    // public boolean removeDupe() {
    //     if (shop.getLocation().getWorld() == null) {
    //         return false;
    //     }
    //     Location displayLoc = shop.getLocation().getBlock().getRelative(0, 1, 0).getLocation();
    //     boolean removed = false;
    //     Chunk c = displayLoc.getChunk();
    //     for (Entity e : c.getEntities()) {
    //         if (!(e instanceof Item)) {
    //             continue;
    //         }
    //         if (this.item != null && e.getEntityId() == this.item.getEntityId()) {
    //             continue;
    //         }
    //         Location eLoc = e.getLocation().getBlock().getLocation();
    //         if (eLoc.equals(displayLoc) || eLoc.equals(shop.getLocation())) {
    //             ItemStack near = ((Item) e).getItemStack();
    //             // if its the same its a dupe
    //             if (this.shop.matches(near)) {
    //                 e.remove();
    //                 removed = true;
    //             }
    //         }
    //     }
    //     return removed;
    //
    // }
    //
    // /**
    //  * Removes the display item.
    //  */
    // public void remove() {
    //     if (this.item == null) {
    //         return;
    //     }
    //     this.item.remove();
    //     this.item = null;
    // }
    //
    // /**
    //  * @return Returns the exact location of the display item. (1 above shop
    //  * block, in the center)
    //  */
    // public Location getDisplayLocation() {
    //     return this.shop.getLocation().clone().add(0.5, 1.2, 0.5);
    // }
    //
    // /**
    //  * Returns the reference to this shops item. Do not modify.
    //  */
    // public Item getItem() {
    //     return this.item;
    // }
    @Override
    public Location getDisplayLocation() {
        return this.shop.getLocation().clone().add(0.5, 1.2, 0.5);
    }
}
