package org.maxgamer.quickshop.Shop;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

public interface DisplayItem {
    public abstract void spawn();

    public abstract void respawn();

    public abstract void safeGuard(Item item);

    public abstract boolean removeDupe();

    public abstract void remove();

    public abstract Location getDisplayLocation();

    public abstract Item getItem();

    public static void checkDisplayMove(Shop shop) {
        if (shop instanceof ContainerShop) {
            ContainerShop cShop = (ContainerShop) shop;
            if (cShop.checkDisplayMoved()) {
                //log("Display item for " + shop
                //        + " is not on the correct location and has been removed. Probably someone is trying to cheat.");
                // for (Player player : getServer().getOnlinePlayers()) {
                //     if (player.hasPermission("quickshop.alerts")) {
                //         player.sendMessage(ChatColor.RED + "[QuickShop] Display item for " + shop
                //                 + " is not on the correct location and has been removed. Probably someone is trying to cheat.");
                //     }
                // }
                Util.sendMessageToOps("[QuickShop] Display item for " + shop + " is not on the correct location and has been removed. Probably someone is trying to cheat.");
                QuickShop.instance.getQueuedShopManager().add(new QueueShopObject(shop, QueueAction.REMOVEDISPLAYITEM));
            }
        }
    }

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
