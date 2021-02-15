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

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Quick access Extra API
 */
@SuppressWarnings("unchecked")
public class ShopExtraManager {
    private final @NotNull Map<String, Object> extra;
    private final @NotNull Shop shop;
    private final @NotNull Plugin namespace;

    public ShopExtraManager(@NotNull Shop shop, @NotNull Plugin namespace) {
        this.extra = shop.getExtra(namespace);
        this.shop = shop;
        this.namespace = namespace;
    }

    @NotNull
    public Set<String> getKeys() {
        return extra.keySet();
    }

    public boolean contains(@NotNull String path) {
        return extra.containsKey(path);
    }


    public boolean isSet(@NotNull String path) {
        return extra.get(path) != null;
    }


    @Nullable
    public Object get(@NotNull String path) {
        return null;
    }


    @Nullable
    public Object get(@NotNull String path, @Nullable Object def) {
        return extra.getOrDefault(path, def);
    }

    public void set(@NotNull String path, @Nullable Object value) {
        extra.put(path, value);
        save();
    }

    @Nullable
    public String getString(@NotNull String path) {
        return (String) extra.get(path);
    }

    @Nullable
    public String getString(@NotNull String path, @Nullable String def) {
        return (String) extra.getOrDefault(path, def);
    }

    public boolean isString(@NotNull String path) {
        return extra.get(path) instanceof String;
    }


    public int getInt(@NotNull String path) {
        return (int) extra.getOrDefault(path, 0);
    }


    public int getInt(@NotNull String path, int def) {
        return (int) extra.getOrDefault(path, def);
    }

    public boolean isInt(@NotNull String path) {
        return extra.get(path) instanceof Integer;
    }


    public boolean getBoolean(@NotNull String path) {
        return (boolean) extra.getOrDefault(path, false);
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        return (boolean) extra.getOrDefault(path, def);
    }

    public boolean isBoolean(@NotNull String path) {
        return extra.get(path) instanceof Boolean;
    }


    public double getDouble(@NotNull String path) {
        return (double) extra.getOrDefault(path, 0d);
    }


    public double getDouble(@NotNull String path, double def) {
        return (double) extra.getOrDefault(path, def);
    }


    public boolean isDouble(@NotNull String path) {
        return extra.get(path) instanceof Double;
    }


    public long getLong(@NotNull String path) {
        return (long) extra.getOrDefault(path, 0L);
    }


    public long getLong(@NotNull String path, long def) {
        return (long) extra.getOrDefault(path, def);
    }


    public boolean isLong(@NotNull String path) {
        return extra.get(path) instanceof Long;
    }


    @Nullable
    public List<?> getList(@NotNull String path) {
        return (List<?>) extra.get(path);
    }


    @Nullable

    public List<?> getList(@NotNull String path, @Nullable List<?> def) {
        return (List<?>) extra.getOrDefault(path, def);

    }


    public boolean isList(@NotNull String path) {
        return extra.get(path) instanceof List;
    }


    @NotNull
    public List<String> getStringList(@NotNull String path) {

        return (List<String>) extra.getOrDefault(path, new ArrayList<>());
    }

    @NotNull
    public List<Integer> getIntegerList(@NotNull String path) {

        return (List<Integer>) extra.getOrDefault(path, new ArrayList<>());
    }

    @NotNull
    public List<Boolean> getBooleanList(@NotNull String path) {

        return (List<Boolean>) extra.getOrDefault(path, new ArrayList<>());
    }


    @NotNull

    public List<Double> getDoubleList(@NotNull String path) {
        return (List<Double>) extra.getOrDefault(path, new ArrayList<>());
    }

    @NotNull

    public List<Float> getFloatList(@NotNull String path) {
        return (List<Float>) extra.getOrDefault(path, new ArrayList<>());

    }

    @NotNull

    public List<Long> getLongList(@NotNull String path) {
        return (List<Long>) extra.getOrDefault(path, new ArrayList<>());
    }


    @NotNull

    public List<Byte> getByteList(@NotNull String path) {
        return (List<Byte>) extra.getOrDefault(path, new ArrayList<>());
    }

    @NotNull

    public List<Character> getCharacterList(@NotNull String path) {
        return (List<Character>) extra.getOrDefault(path, new ArrayList<>());
    }

    @NotNull

    public List<Short> getShortList(@NotNull String path) {
        return (List<Short>) extra.getOrDefault(path, new ArrayList<>());
    }

    @NotNull

    public List<Map<?, ?>> getMapList(@NotNull String path) {
        return (List<Map<?, ?>>) extra.getOrDefault(path, new ArrayList<>());
    }

    @Nullable
    public <T> T getObject(@NotNull String path, @NotNull Class<T> clazz) {
        return (T) extra.get(path);
    }


    @Nullable

    public <T> T getObject(@NotNull String path, @NotNull Class<T> clazz, @Nullable T def) {
        T t = (T) extra.get(path);
        if (t == null)
            return def;
        return t;
    }

    public void save() {
        shop.setExtra(namespace, extra);
    }
}
