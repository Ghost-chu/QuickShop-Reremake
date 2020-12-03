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
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UuidNameCache extends TimerTask {
    private final YamlConfiguration fileCache;
    private final File file;
    private final LoadingCache<UUID, String> cacheMap;
    private final QuickShop plugin;
    private final CacheLoader<UUID, String> cacheLoader;

    public UuidNameCache(QuickShop plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playercache.yml");
        if (!this.file.exists()) {
            try {
                this.file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.fileCache = YamlConfiguration.loadConfiguration(this.file);
        this.cacheLoader = new CacheLoader<UUID, String>() {
            @Override
            public String load(@NotNull UUID key) {
                return Bukkit.getOfflinePlayer(key).getName();
            }
        };
        this.cacheMap = CacheBuilder.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(3, TimeUnit.DAYS)
                .build(cacheLoader);
        new Timer("Auto cache flush").scheduleAtFixedRate(this, 30000, 30000);
    }

    /**
     * Cache player by args
     *
     * @param uuid Player UUID
     * @param name Player name
     */
    public void cachePlayer(@NotNull UUID uuid, @NotNull String name) {
        this.cacheMap.put(uuid, name);
    }

    /**
     * Cache player from Bukkit server built-in caching
     *
     * @param uuid Player UUID
     */
    public void cachePlayerFromServer(@NotNull UUID uuid) {
        this.cacheMap.refresh(uuid); //Call loader to load from bukkit server caching
    }

    /**
     * Get player name from uuid
     *
     * @param uuid Player UUID
     * @return returns player name or null for no results
     */
    @SneakyThrows
    public @Nullable String getName(@NotNull UUID uuid) {
        return this.cacheMap.getIfPresent(uuid);
    }

    /**
     * Get player uuid from name
     *
     * @param name Player name
     * @return returns player uuid or null for no results
     */
    @SneakyThrows
    public @Nullable UUID getUuid(@NotNull String name) {
        for (Map.Entry<UUID, String> set : this.cacheMap.asMap().entrySet()) {
            if (set.getValue().equals(name)) {
                return set.getKey();
            }
        }
        return null;
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        try {
            fileCache.set("cache", null);
            this.cacheMap.asMap().forEach((key, value) -> {
                fileCache.set("cache." + key.toString().replace("-", ""), value);
            });
            fileCache.save(this.file);
        } catch (IOException e) {
            Util.debugLog("Failed to save the name caches: " + e.getMessage());
        }
    }
}
