/*
 * This file is a part of project QuickShop, the name is PriceLimiter.java
 *  Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@AllArgsConstructor
@Data
public class PriceLimiter {
    private double minPrice;
    private double maxPrice;
    private boolean allowFreeShop;

    @NotNull
    public Status check(@NotNull ItemStack stack, double price) {
        if (allowFreeShop) {
            if (price != 0 && price < minPrice) {
                return Status.REACHED_PRICE_MIN_LIMIT;
            }
        }
        if (price < minPrice) {
            return Status.REACHED_PRICE_MIN_LIMIT;
        }
        if (maxPrice != -1) {
            if (price > maxPrice) {
                return Status.REACHED_PRICE_MAX_LIMIT;
            }
        }
        double perItemPrice = CalculateUtil.subtract(price, stack.getAmount());
        Map.Entry<Double, Double> materialLimit = Util.getPriceRestriction(stack.getType());
        if (materialLimit != null) {
            if (perItemPrice < materialLimit.getKey() || perItemPrice > materialLimit.getValue()) {
                return Status.PRICE_RESTRICTED;
            }
        }
        return Status.PASS;
    }

    public enum Status {
        PASS,
        REACHED_PRICE_MAX_LIMIT,
        REACHED_PRICE_MIN_LIMIT,
        PRICE_RESTRICTED
    }
}
