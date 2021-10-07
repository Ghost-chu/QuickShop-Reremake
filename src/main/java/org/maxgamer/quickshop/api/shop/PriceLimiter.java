package org.maxgamer.quickshop.api.shop;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Utility used for shop price validating
 */
public interface PriceLimiter {
    /**
     * Checks a stack with a price is allowed
     * @param stack The item
     * @param price The price
     * @return Allowed
     */
    @NotNull
    PriceLimiterCheckResult check(@NotNull ItemStack stack, double price);

}
