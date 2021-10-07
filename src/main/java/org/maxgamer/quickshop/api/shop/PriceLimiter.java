package org.maxgamer.quickshop.api.shop;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface PriceLimiter {

    @NotNull
    PriceLimiterCheckResult check(@NotNull ItemStack stack, double price);

}
