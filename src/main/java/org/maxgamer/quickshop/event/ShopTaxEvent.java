/*
 * This file is a part of project QuickShop, the name is ShopTaxEvent.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.event;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.shop.Shop;

import java.util.UUID;

public class ShopTaxEvent extends QSEvent {
    @Getter
    private final UUID user;
    @Getter
    private final Shop shop;
    @Getter
    @Setter
    private double tax;

    /**
     * Call when shop calc shop tax that will pay to system account and remove from shop owner/player received money
     *
     * @param shop The shop
     * @param tax  The tax
     * @param user The user (buyer/seller)
     */
    public ShopTaxEvent(@NotNull Shop shop, double tax, @NotNull UUID user) {
        this.shop = shop;
        this.tax = tax;
        this.user = user;
    }


}
