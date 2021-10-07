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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.api.shop.Shop;

import java.util.concurrent.TimeUnit;

/**
 * Cache is a utilities to quick access shops on large network with Caffeine Cache Library
 *
 * @author Ghost_chu
 */
public class Cache {
    private final QuickShop plugin;
    private final com.google.common.cache.Cache<Location, Shop> accessCaching = CacheBuilder
            .newBuilder()
            .initialCapacity(1000)
            .expireAfterAccess(120, TimeUnit.MINUTES)
            .weakValues()
            .recordStats()
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
        Shop shop = accessCaching.getIfPresent(location);
        if (shop == null) {
            if (includeAttached) {
                shop = plugin.getShopManager().getShopIncludeAttached(location, false);
            } else {
                shop = plugin.getShopManager().getShop(location);
            }
        }
        if (shop != null) {
            setCache(location, shop);
        }
        return shop;
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
