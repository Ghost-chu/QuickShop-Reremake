/*
 * This file is a part of project QuickShop, the name is ShopSuccessPurchaseEvent.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopType;

public class ShopSuccessPurchaseEvent extends QSEvent implements Cancellable {

    @Getter
    private final int amount;

    @Getter
    @NotNull
    private final Player player;

    @Getter
    @NotNull
    private final Shop shop;

    @Getter
    private final double tax;

    private final double total; //Don't use getter, we have important notice need told dev in javadoc.

    private boolean cancelled;
   

    /**
     * Builds a new shop purchase event
     * This time, purchase not start, please listen the ShopSuccessPurchaseEvent.
     *
     * @param shop   The shop bought from
     * @param player The player buying
     * @param amount The amount they're buying
     * @param tax    The tax in this purchase
     * @param total  The money in this purchase
     */
    public ShopSuccessPurchaseEvent(@NotNull Shop shop, @NotNull Player player, int amount, double total, double tax) {
        this.shop = shop;
        this.player = player;
        this.amount = amount;
        this.tax = tax;
        this.total = total;
    }


    /**
     * The total money changes in this purchase.
     * Calculate tax, if you want get total without tax, please use getBalanceWithoutTax()
     *
     * @return the total money with calculate tax
     */
    public double getBalance() {
        return this.total * (1 - tax);
    }

    /**
     * The total money changes in this purchase.
     * No calculate tax, if you want get total with tax, please use getBalance()
     *
     * @return the total money without calculate tax
     */
    public double getBalanceWithoutTax() {
        return this.total;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
