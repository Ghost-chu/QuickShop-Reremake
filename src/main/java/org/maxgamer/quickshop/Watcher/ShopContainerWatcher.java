/*
 * This file is a part of project QuickShop, the name is ShopContainerWatcher.java
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

import java.util.LinkedList;
import java.util.Queue;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.ContainerShop;
import org.maxgamer.quickshop.Shop.Shop;

/**
 * Check the shops after server booted up, make sure shop can correct self-deleted when container
 * lost.
 */
// unused, pending to remove
public class ShopContainerWatcher extends BukkitRunnable {
  // private QuickShop plugin;
  private Queue<Shop> checkQueue = new LinkedList<>();

  public ShopContainerWatcher(@NotNull QuickShop plugin) {
    // this.plugin = plugin;
  }

  public void scheduleCheck(@NotNull Shop shop) {
    checkQueue.add(shop);
  }

  @Override
  public void run() {
    long beginTime = System.currentTimeMillis();
    Shop shop = checkQueue.poll();
    while (shop != null) {
      if (shop instanceof ContainerShop) {
        ((ContainerShop) shop).checkContainer();
      }
      if (System.currentTimeMillis() - beginTime
          > 45) { // Don't let quickshop eat more than 45 ms per tick.
        break;
      }
      shop = checkQueue.poll();
    }
  }
}
