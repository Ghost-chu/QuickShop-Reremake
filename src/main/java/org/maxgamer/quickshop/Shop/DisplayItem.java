package org.maxgamer.quickshop.Shop;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public interface DisplayItem {
    /**
     * Spawn the displayItem
     */
    public abstract void spawn();

    /**
     * Respawn the displayItem
     */
    public abstract void respawn();

    /**
     * Protect the target Item and mark is QS's shop item.
     *
     * @param item The item you want protect.
     */
    public abstract void safeGuard(Item item);

    /**
     * Remove the dupe items.
     * @return Had dupes removed.
     */
    public abstract boolean removeDupe();

    /**
     * Remove the DisplayItem
     */
    public abstract void remove();

    /**
     * Get the displayLocation
     * @return DisplayItem's location, not promise target location have the display item.
     */
    public abstract Location getDisplayLocation();

    /**
     * Get the displayItem's item, maybe return null
     * @return The displayItem's item, maybe null when it not exist.
     */
    public abstract Item getItem();

    /**
     * Check the display is or not moved.
     * @param shop shop
     */
    public abstract boolean checkDisplayMoved(Shop shop);

    /**
     * Check the ItemStack is or not a DisplayItem
     *
     * @param itemStack The ItemStack you want to check.
     * @return The check result.
     */
    public static boolean checkShopItem(ItemStack itemStack) {
        if (itemStack == null)
            return false;
        if (!itemStack.hasItemMeta()) {
            return false;
        }
        if (itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().contains("QuickShop")) {
            return true;
        }
        if (itemStack.getItemMeta().hasLore()) {
            List<String> lores = itemStack.getItemMeta().getLore();
            for (String singleLore : lores) {
                if (singleLore.equals("QuickShop DisplayItem") || singleLore.contains("QuickShop DisplayItem")) {
                    return true;
                }
            }
        }
        return false;
    }

}
