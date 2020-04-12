package org.maxgamer.quickshop.util;

import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@AllArgsConstructor
public class PriceLimiter {
    private double minPrice;
    private double maxPrice;
    private boolean allowFreeShop;

    @NotNull
    public Status check(@NotNull ItemStack stack, double price){
        if (allowFreeShop) {
            if (price != 0 && price < minPrice) {
                return Status.REACHED_PRICE_MIN_LIMIT;
            }
        }
        if (price < minPrice) {
            return Status.REACHED_PRICE_MIN_LIMIT;
        }
        if(maxPrice != -1){
            if(price > maxPrice){
                return Status.REACHED_PRICE_MAX_LIMIT;
            }
        }
        Map.Entry<Double, Double> materialLimit = Util.getPriceRestriction(stack.getType());
        if (materialLimit != null) {
            if (price < materialLimit.getKey() || price > materialLimit.getValue()) {
               return Status.PRICE_RESTRICTED;
            }
        }
        return Status.PASS;
    }
}
enum Status{
    PASS,
    REACHED_PRICE_MAX_LIMIT,
    REACHED_PRICE_MIN_LIMIT,
    REACHED_DIGITS_LIMIT,
    PRICE_RESTRICTED
}