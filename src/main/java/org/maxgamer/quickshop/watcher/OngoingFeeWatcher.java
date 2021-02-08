/*
 * This file is a part of project QuickShop, the name is OngoingFeeWatcher.java
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

package org.maxgamer.quickshop.watcher;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

/**
 * Check the shops after server booted up, make sure shop can correct
 * self-deleted when container lost.
 */
public class OngoingFeeWatcher extends BukkitRunnable {
  private final QuickShop plugin;

  public OngoingFeeWatcher(@NotNull QuickShop plugin) { this.plugin = plugin; }

  @Override
  public void run() {
    Util.debugLog("Run task for ongoing fee...");
    if (plugin.getEconomy() == null) {
      Util.debugLog("Economy hadn't get ready.");
      return;
    }
    int cost = plugin.getConfig().getInt("shop.ongoing-fee.cost-per-shop");
    boolean allowLoan =
        plugin.getConfig().getBoolean("shop.allow-economy-loan");
    boolean ignoreUnlimited =
        plugin.getConfig().getBoolean("shop.ongoing-fee.ignore-unlimited");
    for (Shop shop : plugin.getShopManager().getAllShops()) {
      if ((!shop.isUnlimited() || !ignoreUnlimited) && !shop.isDeleted()) {
        UUID shopOwner = shop.getOwner();
        Bukkit.getScheduler().runTask(plugin, () -> {
          if (!allowLoan && (plugin.getEconomy().getBalance(
                                 shopOwner, shop.getLocation().getWorld(),
                                 shop.getCurrency()) < cost)) { // Disallow loan
            this.removeShop(shop);
          }
          boolean success = plugin.getEconomy().withdraw(
              shop.getOwner(), cost, shop.getLocation().getWorld(),
              shop.getCurrency());
          if (!success) {
            this.removeShop(shop);
          } else {
            try {
              // noinspection ConstantConditions,deprecation
              plugin.getEconomy().deposit(
                  Bukkit.getOfflinePlayer(plugin.getConfig().getString("tax"))
                      .getUniqueId(),
                  cost, shop.getLocation().getWorld(), shop.getCurrency());
            } catch (Exception ignored) {
            }
          }
        });
      } else {
        Util.debugLog(
            "Shop was ignored for ongoing fee cause it is unlimited and ignoreUnlimited = true : " +
            shop);
      }
    }
  }

  /**
   * Remove shop and send alert to shop owner
   *
   * @param shop The shop was remove cause no enough ongoing fee
   */
  public void removeShop(@NotNull Shop shop) {
    Bukkit.getScheduler().runTask(plugin, (@NotNull Runnable)shop::delete);
    MsgUtil.send(shop, shop.getOwner(),
                 MsgUtil.getMessageOfflinePlayer(
                     "shop-removed-cause-ongoing-fee",
                     Bukkit.getOfflinePlayer(shop.getOwner()),
                     "World:" +
                         Objects.requireNonNull(shop.getLocation().getWorld())
                             .getName() +
                         " X:" + shop.getLocation().getBlockX() +
                         " Y:" + shop.getLocation().getBlockY() +
                         " Z:" + shop.getLocation().getBlockZ()));
  }
}
