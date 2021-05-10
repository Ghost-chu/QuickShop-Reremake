/*
 * This file is a part of project QuickShop, the name is QuickShopAPI.java
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

package org.maxgamer.quickshop.api.v2;

import lombok.AllArgsConstructor;
import org.apache.commons.compress.utils.Lists;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.shop.Shop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class QuickShopAPI {
    private QuickShop plugin;

    public static QuickShopAPI get() {
        return new QuickShopAPI(QuickShop.getInstance());
    }

    public void setup(QuickShop p) {
        plugin = p;
    }

    public List<Shop> getAllShops() {
        return plugin.getShopManager().getAllShops();
    }

    public List<Shop> getLoadedShops() {
        return new ArrayList<>(plugin.getShopManager().getLoadedShops());
    }

    public List<Shop> getShops(World world) {
        return plugin.getShopManager().getShopsInWorld(world);
    }

    public List<Shop> getShops(UUID player) {
        return plugin.getShopManager().getPlayerAllShops(player);
    }

    public List<Shop> getShops(World world, UUID player) {
        return getShops(world).stream().filter(shop -> shop.getOwner().equals(player)).collect(Collectors.toList());
    }

    public List<Shop> getShops(Chunk chunk) {
        Map<Location, Shop> mapping = plugin.getShopManager().getShops(chunk);
        return mapping == null ? Lists.newArrayList() : new ArrayList<>(mapping.values());
    }


}
