/*
 * This file is a part of project QuickShop, the name is Cache.java
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

package org.maxgamer.quickshop;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.shop.Shop;

import java.util.concurrent.TimeUnit;

public class Cache {
    private final QuickShop plugin;
    private final com.github.benmanes.caffeine.cache.Cache<Location, Shop> accessCaching = Caffeine
            .newBuilder()
            .initialCapacity(10000)
            .expireAfterAccess(120, TimeUnit.MINUTES)
            .recordStats()
            .weakValues()
            .build();

    public Cache(QuickShop plugin) {
        this.plugin = plugin;
    }

    public @NonNull CacheStats getStats() {
        return accessCaching.stats();
    }


    /**
     * Gets shop from plugin caching
     *
     * @param location        The shop location that you want to get
     * @param includeAttached Include attached shops
     * @return The shop, null for no shops found in caching and memory
     */
    @Nullable
    public Shop getCaching(@NotNull Location location, boolean includeAttached) {
        Shop result = accessCaching.get(location, update -> {
            Shop shop; //Because we need the data from Caffeine, so we cannot direct return WeakReference directly
            //Cause we will see 100% load success data :(
            if (includeAttached) {
                shop = plugin.getShopManager().getShopIncludeAttached(update, false);
            } else {
                shop = plugin.getShopManager().getShop(update);
            }
            return shop;
        });
        return result;
    }

    /**
     * Update and invalidate the caching
     *
     * @param location The location that you want to update
     * @param shop     null for invalidate and Shop object for update
     */
    public void setCache(@NotNull Location location, @Nullable Shop shop) {
        if (shop == null) {
            accessCaching.invalidate(location);
            return;
        }
        accessCaching.put(location, shop);
    }
}
