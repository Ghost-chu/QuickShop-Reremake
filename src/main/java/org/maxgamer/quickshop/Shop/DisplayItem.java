package org.maxgamer.quickshop.Shop;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.maxgamer.quickshop.QuickShop;
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


    /**
     * Get display should at location.
     * Not display current location.
     * @return Should at
     */
    public abstract Location getDisplayLocation();

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
     * Check the display is or not already spawned
     *
     * @return Spawned
     */
    public abstract boolean isSpawned();

    /**
     * Create a new itemStack with protect flag.
     * @param itemStack Old itemStack
     * @return New itemStack with protect flag.
     */
    public static ItemStack createGuardItemStack(ItemStack itemStack, Shop shop) {
        itemStack = itemStack.clone();
        ItemMeta iMeta = itemStack.getItemMeta();
        if (QuickShop.instance.getConfig().getBoolean("shop.display-item-use-name")) {
            iMeta.setDisplayName("QuickShop DisplayItem");
        }
        java.util.List<String> lore = new ArrayList<String>();
        for (int i = 0; i < 21; i++) {
            lore.add("QuickShop DisplayItem#" + shop
                    .hashCode()); //Create 20 lines lore to make sure no stupid plugin accident remove mark.
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
     * Check the itemStack is target shop's display
     *
     * @param itemStack Target ItemStack
     * @param shop      Target shop
     * @return Is target shop's display
     */
    public static boolean checkIsTargetShopDisplay(ItemStack itemStack, Shop shop) {
        if (itemStack == null)
            return false;
        itemStack = itemStack.clone();
        if (!itemStack.hasItemMeta())
            return false;
        ItemMeta iMeta = itemStack.getItemMeta();
        if (!iMeta.hasLore())
            return false;
        List<String> lores = iMeta.getLore();
        for (String lore : lores) {
            if (lore.toLowerCase().contains("quickshop displayitem")) {
                if (lore.equals("quickshop displayitem#" + shop.hashCode()))
                    return true;
            }
        }
        return false;
    }

    /**
     * Get plugin now is using which one DisplayType
     * @return Using displayType.
     */
    public static DisplayType getNowUsing() {
        return DisplayType.fromID(QuickShop.instance.getConfig().getInt("shop.display-type"));
    }

}