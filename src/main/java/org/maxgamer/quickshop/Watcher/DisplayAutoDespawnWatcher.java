/*
 * This file is a part of project QuickShop, the name is DisplayAutoDespawnWatcher.java
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

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

@AllArgsConstructor
public class DisplayAutoDespawnWatcher extends BukkitRunnable {
    private QuickShop plugin;

    @Override
    public void run() {
        if (plugin.getShopManager().getLoadedShops() == null) {
            return;
        }

        plugin.getShopManager().getLoadedShops().parallelStream().forEach(shop -> {
            //Check the range has player?
            int range = plugin.getConfig().getInt("shop.display-despawn-range");
            boolean anyPlayerInRegion = Bukkit.getOnlinePlayers()
                    .parallelStream()
                    .filter(player -> player.getWorld().equals(shop.getLocation().getWorld()))
                    .anyMatch(player -> player.getLocation().distance(shop.getLocation()) < range);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (shop.getDisplay() == null) {
                        return;
                    }
                    if (anyPlayerInRegion) {
                        if (!shop.getDisplay().isSpawned()) {
                            Util.debugLog("Respawning the shop " + shop + " the display, cause it was despawned and a player close it");
                            shop.checkDisplay();
                        }
                    } else {
                        if (shop.getDisplay().isSpawned()) {
                            Util.debugLog("Removing the shop " + shop + " the display, cause nobody can see it");
                            shop.getDisplay().remove();
                        }
                    }
                }
            }.runTask(QuickShop.instance);
        });
    }
}
