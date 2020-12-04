/*
 * This file is a part of project QuickShop, the name is UuidNameCache.java
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

package org.maxgamer.quickshop.util.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UUIDNameCache {
    private final LoadingCache<UUID, String> uuid2NameCache;

    public UUIDNameCache() {
        this.uuid2NameCache = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(2, TimeUnit.DAYS)
                .build(new CacheLoader<UUID, String>() {
                    @Override
                    public String load(@NotNull UUID key) {
                        String name = Bukkit.getOfflinePlayer(key).getName();
                        return name == null ? "" : name;
                    }
                });
    }

    /**
     * Cache player from Bukkit server built-in caching
     *
     * @param uuid Player UUID
     */
    public void cachePlayerFromServer(@NotNull UUID uuid) {
        //TODO:refresh when player name changed, or just refresh when player join?
        this.uuid2NameCache.refresh(uuid); //Call loader to load from bukkit server caching
    }

    /**
     * Get player name from uuid
     *
     * @param uuid Player UUID
     * @return returns player name or null for no results
     */
    @SneakyThrows
    public @Nullable String getName(@NotNull UUID uuid) {
        String name = uuid2NameCache.getIfPresent(uuid);
        if (name == null) {
            uuid2NameCache.refresh(uuid);
            return getName(uuid);
        } else {
            return name.isEmpty() ? null : name;
        }
    }
}
