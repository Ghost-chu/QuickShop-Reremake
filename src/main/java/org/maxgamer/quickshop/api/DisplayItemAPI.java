package org.maxgamer.quickshop.api;

import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.shop.DisplayItem;
import org.maxgamer.quickshop.shop.DisplayType;
import org.maxgamer.quickshop.shop.Shop;

@AllArgsConstructor
public class DisplayItemAPI {
    private QuickShop plugin;

    /**
     * Checks is a display item
     * @param itemStack The itemstack
     * @return yes or no
     */
    public static boolean isDisplayItem(@NotNull ItemStack itemStack){
        return DisplayItem.checkIsGuardItemStack(itemStack);
    }

    /**
     * Check is a shop's display item
     * @param itemStack The itemstack
     * @param shop The itemstack
     * @return yes or no
     */
    public static boolean isShopDisplayItem(@NotNull ItemStack itemStack, @NotNull Shop shop){
        return DisplayItem.checkIsTargetShopDisplay(itemStack,shop);
    }

    /**
     * Gets the display type now using
     * @return The type of display now using
     */
    public static DisplayType getNowUsing(){
        return DisplayItem.getNowUsing();
    }
}
