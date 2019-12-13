/*
 * This file is a part of project QuickShop, the name is ShopVaildWatcher.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Watcher;

import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.Util;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Check the shops after server booted up, make sure shop can correct self-deleted when container lost.
 */
public class ShopVaildWatcher extends BukkitRunnable {
    private QuickShop plugin;
    private Queue<Shop> checkQueue = new LinkedList<>();

    public ShopVaildWatcher(@NotNull QuickShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int checkedShops = 0;
        int maxCheckShops = plugin.getConfig().getInt("shop.max-shops-checks-in-once");
        Shop shop = checkQueue.poll();
        while (shop != null) {
            if (shop.isLoaded() && !shop.isValid()) {
                shop.delete();
                Util.debugLog("Removed shop at " + shop.getLocation() + " cause the container is missing or not a usable container.");
            }
            checkedShops++;
            if (checkedShops >= maxCheckShops) {
                Util.debugLog("Shop check reached the limit, force exit and wait next check window.");
            }
            shop = checkQueue.poll();
        }
    }
}