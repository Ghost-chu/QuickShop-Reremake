/*
 * This file is a part of project QuickShop, the name is ShopAPI.java
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

package org.maxgamer.quickshop.api;

import lombok.AllArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.shop.Shop;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ShopAPI {
    private final QuickShop plugin;

    /**
     * Gets the shops on this server
     *
     * @return All shops included unloaded and loaded
     */
    public List<Shop> getAllShops() {
        return plugin.getShopManager().getAllShops();
    }

    /**
     * Gets the shops that loaded
     *
     * @return The loaded shops
     */
    public List<Shop> getLoadedShops() {
        return new ArrayList<>(plugin.getShopManager().getLoadedShops());
    }

    /**
     * Gets shops in specific world
     *
     * @param world world
     * @return The shops in specific world
     */
    public List<Shop> getShops(World world) {
        return plugin.getShopManager().getShopsInWorld(world);
    }

    /**
     * Gets shops owned by specific player
     *
     * @param player shop owner
     * @return The shops owned by specific player
     */
    public List<Shop> getShops(UUID player) {
        return plugin.getShopManager().getPlayerAllShops(player);
    }

    /**
     * Gets shops owned by specific player and in specific world
     *
     * @param world  world
     * @param player shop owner
     * @return shop collection
     */
    public List<Shop> getShops(World world, UUID player) {
        return plugin.getShopManager().getPlayerAllShops(player).stream().filter(shop -> Objects.requireNonNull(shop.getLocation().getWorld()).equals(world)).collect(Collectors.toList());
    }

    /**
     * Gets shops in specific chunk
     *
     * @param chunk the chunk
     * @return chunk
     */
    public List<Shop> getShops(Chunk chunk) {
        Map<Location, Shop> mapping = plugin.getShopManager().getShops(chunk);
        return mapping == null ? Lists.newArrayList() : new ArrayList<>(mapping.values());
    }

    /**
     * Check if block is a shop and get it
     *
     * @param block The block
     * @return Shop object if target is a shop otherwise null
     */
    public Optional<Shop> getShop(Block block) {
        return Optional.ofNullable(plugin.getShopManager().getShopIncludeAttached(block.getLocation()));
    }

    /**
     * Check if location block is a shop and get it
     *
     * @param location The block location
     * @return Shop object if target is a shop otherwise null
     */
    public Optional<Shop> getShop(Location location) {
        return Optional.ofNullable(plugin.getShopManager().getShopIncludeAttached(location));
    }
}
