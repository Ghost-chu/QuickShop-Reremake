package org.maxgamer.quickshop.Shop;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.maxgamer.quickshop.QuickShop;
//import org.maxgamer.quickshop.Util.NMS;

/**
 * @author Netherfoam A display item, that spawns a block above the chest and
 * cannot be interacted with.
 */
public interface DisplayItem {
    // private Shop shop;
    // private ItemStack iStack;
    // private Item item;
    // static QuickShop plugin = QuickShop.instance;
    // private Location displayLoc;

    // /**
    //  * ZZ
    //  * Creates a new display item.
    //  *
    //  * @param shop   The shop (See Shop)
    //  * @param iStack The item stack to clone properties of the display item from.
    //  */
    // public DisplayItem(Shop shop, ItemStack iStack) {
    //     this.shop = shop;
    //     this.iStack = iStack.clone();
    //
    //     // this.displayLoc = shop.getLocation().clone().add(0.5, 1.2, 0.5);
    // }

    public abstract void spawn();

    // /**
    //  * Spawns the dummy item on top of the shop.
    //  */
    // public void spawn() {
    //     if (shop.getLocation().getWorld() == null)
    //         return;
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

    // /**
    //  * Spawns the new display item. Does not remove duplicate items.
    //  */
    // public void respawn() {
    //     remove();
    //     spawn();
    // }

    public abstract void respawn();

    public abstract void safeGuard(Entity entity);
    // /**
    //  * Set item is QuickShop's DisplayItem and prevent them.
    //  *
    //  * @param item
    //  */
    // public void safeGuard(Item item) {
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

    public abstract boolean checkIsShopEntity(ItemStack itemStack);

    public abstract boolean checkIsShopEntity(Entity entity);

    // /**
    //  * Check the ItemStack is or not a DisplayItem
    //  * @param itemStack The ItemStack you want to check.
    //  * @return The check result.
    //  */
    //
    // public static boolean checkShopItem(ItemStack itemStack) {
    //     if (itemStack == null)
    //         return false;
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

    public abstract boolean removeDupe();

    // /**
    //  * Removes all items floating ontop of the chest that aren't the display
    //  * item.
    //  */
    // public boolean removeDupe() {
    //     if (shop.getLocation().getWorld() == null)
    //         return false;
    //     Location displayLoc = shop.getLocation().getBlock().getRelative(0, 1, 0).getLocation();
    //     boolean removed = false;
    //     Chunk c = displayLoc.getChunk();
    //     for (Entity e : c.getEntities()) {
    //         if (!(e instanceof Item))
    //             continue;
    //         if (this.item != null && e.getEntityId() == this.item.getEntityId())
    //             continue;
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

    public abstract void remove();

    // /**
    //  * Removes the display item.
    //  */
    // public void remove() {
    //     if (this.item == null)
    //         return;
    //     this.item.remove();
    //     this.item = null;
    // }

    public abstract Location getDisplayLocation();

    // /**
    //  * @return Returns the exact location of the display item. (1 above shop
    //  * block, in the center)
    //  */
    // public Location getDisplayLocation() {
    //     return this.shop.getLocation().clone().add(0.5, 1.2, 0.5);
    // }
    //

    public abstract Entity getDisplay();

    // /**
    //  * Returns the reference to this shops item. Do not modify.
    //  */
    // public Item getItem() {
    //     return this.item;
    // }

    public abstract boolean checkDisplayIsMoved();

    public abstract boolean checkDisplayNeedRegen();

    public abstract void fixDisplayMoved();

    public abstract void fixDisplayNeedRegen();

    public static ItemStack createGuardItemStack(ItemStack itemStack) {
        itemStack = itemStack.clone();
        ItemMeta iMeta = itemStack.getItemMeta();
        if (QuickShop.instance.getConfig().getBoolean("shop.display-item-use-name")) {
            iMeta.setDisplayName("QuickShop DisplayItem");
        }
        java.util.List<String> lore = new ArrayList<String>();
        for (int i = 0; i < 21; i++) {
            lore.add("QuickShop DisplayItem"); //Create 20 lines lore to make sure no stupid plugin accident remove mark.
        }
        iMeta.setLore(lore);
        itemStack.setItemMeta(iMeta);
        return itemStack;
    }

    public static boolean checkIsGuardItemStack(ItemStack itemStack) {
        itemStack = itemStack.clone();
        if (!itemStack.hasItemMeta())
            return false;
        ItemMeta iMeta = itemStack.getItemMeta();
        if (iMeta.hasDisplayName()) {
            if (iMeta.getDisplayName().toLowerCase().contains("quickshop displayitem"))
                return true;
        }
        if (iMeta.hasLore()) {
            List<String> lores = iMeta.getLore();
            for (String lore : lores) {
                if (lore.toLowerCase().contains("quickshop displayitem")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static DisplayType getNowUsing() {
        //TODO
    }

}