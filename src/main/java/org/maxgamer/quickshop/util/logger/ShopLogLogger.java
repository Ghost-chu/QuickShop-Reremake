/*
 * This file is a part of project QuickShop, the name is ShopLogLogger.java
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

package org.maxgamer.quickshop.util.logger;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.economy.EconomyTransaction;
import org.maxgamer.quickshop.shop.Shop;

import java.util.UUID;

// TODO
public interface ShopLogLogger {
    void recordPurchase(@NotNull UUID trader, @NotNull Shop shop, @NotNull EconomyTransaction transaction, long timestamp);

    void recordShopDeleting(@NotNull Shop shop, long timestamp);

    void recordShopCreating(@NotNull Shop shop, long timestamp);

    void recordShopChanging(@NotNull Shop shop, long timestamp);

    void recordAddonEvent(@NotNull Plugin plugin, @NotNull String jsonContent, long timestamp);
}
