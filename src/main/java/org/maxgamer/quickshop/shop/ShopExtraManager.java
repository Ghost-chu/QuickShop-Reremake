/*
 * This file is a part of project QuickShop, the name is ShopExtraManager.java
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

package org.maxgamer.quickshop.shop;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.util.JsonUtil;
import org.maxgamer.quickshop.util.Util;

import java.util.List;
import java.util.Map;

/**
 * Quick access Extra API
 */
public class ShopExtraManager {
    private final Shop shop;
    private final Plugin namespace;

    ShopExtraManager(@NotNull Shop shop, @NotNull Plugin namespace) {
        this.shop = shop;
        this.namespace = namespace;
    }

    public Integer getInteger(@NotNull String key, @Nullable Integer def) {
        return Integer.parseInt(this.shop.getExtra(namespace).getOrDefault(key, String.valueOf(def)));
    }

    public Double getDouble(@NotNull String key, @Nullable Double def) {
        return Double.parseDouble(this.shop.getExtra(namespace).getOrDefault(key, String.valueOf(def)));
    }

    public String getString(@NotNull String key, @Nullable String def) {
        return this.shop.getExtra(namespace).getOrDefault(key, def);
    }

    public List<String> getStringList(@NotNull String key, @Nullable List<String> def) {
        String listString = this.shop.getExtra(namespace).get(key);
        if (listString == null) {
            return def;
        }
        //noinspection unchecked
        return (List<String>) JsonUtil.getGson().fromJson(listString, List.class);
    }

    public ItemStack getItemStack(@NotNull String key, @Nullable ItemStack def) {
        String stackStr = this.shop.getExtra(namespace).get(key);
        if (stackStr == null) {
            return def;
        }
        try {
            return Util.deserialize(stackStr);
        } catch (InvalidConfigurationException ignored) {
            return null;
        }
    }

    public void set(@NotNull String key, @Nullable Object obj) {
        Map<String, String> map = this.shop.getExtra(namespace);
        if (obj == null) {
            map.remove(key);
        } else {
            if (obj instanceof Integer) {
                map.put(key, String.valueOf(obj));
            } else if (obj instanceof Double) {
                map.put(key, String.valueOf(obj));
            } else if (obj instanceof ItemStack) {
                map.put(key, Util.serialize((ItemStack) obj));
            } else {
                map.put(key, JsonUtil.getGson().toJson(obj));
            }
        }
        this.shop.setExtra(namespace, map);
    }

}
