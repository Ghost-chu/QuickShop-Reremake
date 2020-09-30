/*
 * This file is a part of project QuickShop, the name is ServerUtil.java
 *  Copyright (C) sandtechnology <https://github.com/sandtechnology>
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

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A util related to server
 *
 * @author sandtechnology
 * @since 4.0.4.11
 */
public class ServerUtil {
    private static final Map<String, UUID> UIDCacheMap = new HashMap<>(3);
    private static final String UIDPrefix = "UID";

    private ServerUtil() {
    }

    /**
     * Return the worldNameID for a specific world
     *
     * @param world the world
     * @return a non-space unique string or null if the world is NULL
     */
    @NotNull
    public static String toWorldNameID(@Nullable World world) {
        if (world == null) {
            return "null";
        }
        String result = UIDPrefix + world.getUID();
        UIDCacheMap.merge(result, world.getUID(), (oldVal, newVal) -> newVal);
        return result;
    }

    /**
     * Convert a worldNameID to a specific world
     *
     * @param worldNameID the world unique string, support worldName
     * @return the world instance
     * @throws NullPointerException If world is not found
     */
    @NotNull
    public static World fromWorldNameID(@NotNull String worldNameID) {
        if (worldNameID.startsWith(UIDPrefix)) {
            World world = Bukkit.getWorld(UIDCacheMap.get(worldNameID));
            if (world != null) {
                return world;
            } else {
                Objects.requireNonNull(Bukkit.getWorld(worldNameID));
            }
        }
        return Objects.requireNonNull(Bukkit.getWorld(worldNameID));
    }
}