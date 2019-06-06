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
    /**
     * Spawn new Displays
     */
    public abstract void spawn();

    /**
     * Respawn the displays, if it not exist, it will spawn new one.
     */
    public abstract void respawn();

    /**
     * Add the protect flags for entity or entity's hand item.
     * Target entity will got protect by QuickShop
     *
     * @param entity Target entity
     */
    public abstract void safeGuard(Entity entity);

    /**
     * Check target Entity is or not a QuickShop display Entity.
     * @param entity Target entity
     * @return Is or not
     */
    public abstract boolean checkIsShopEntity(Entity entity);

    /**
     * Remove this shop's display in the whole world.(Not whole server)
     * @return Success
     */
    public abstract boolean removeDupe();

    /**
     * Remove the display entity.
     */
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

    /**
     * Get display should at location.
     * Not display current location.
     * @return Should at
     */
    public abstract Location getDisplayLocation();

    // /**
    //  * @return Returns the exact location of the display item. (1 above shop
    //  * block, in the center)
    //  */
    // public Location getDisplayLocation() {
    //     return this.shop.getLocation().clone().add(0.5, 1.2, 0.5);
    // }
    //

    /**
     * Get the display entity
     * @return Target entity
     */
    public abstract Entity getDisplay();

    /**
     * Check the display is or not moved.
     * @return Moved
     */
    public abstract boolean checkDisplayIsMoved();

    /**
     * Check the display is or not need respawn
     * @return Need
     */
    public abstract boolean checkDisplayNeedRegen();

    /**
     * Fix the display moved issue.
     */
    public abstract void fixDisplayMoved();

    /**
     * Fix display need respawn issue.
     */
    public abstract void fixDisplayNeedRegen();

    /**
     * Create a new itemStack with protect flag.
     * @param itemStack Old itemStack
     * @return New itemStack with protect flag.
     */
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

    /**
     * Check the itemStack is contains protect flag.
     * @param itemStack Target ItemStack
     * @return Contains protect flag.
     */
    public static boolean checkIsGuardItemStack(ItemStack itemStack) {
        if (itemStack == null)
            return false;
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

    /**
     * Get plugin now is using which one DisplayType
     * @return Using displayType.
     */
    public static DisplayType getNowUsing() {
        //TODO Read config and choosen which type we need to use.
        return DisplayType.fromID(QuickShop.instance.getConfig().getInt("shop.display-type"));
    }

}