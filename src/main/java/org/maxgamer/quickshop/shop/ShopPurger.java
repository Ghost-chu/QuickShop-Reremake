/*
 * This file is a part of project QuickShop, the name is ShopPurger.java
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

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.economy.EconomyTransaction;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.Util;

import java.time.temporal.ChronoUnit;

@AllArgsConstructor
public class ShopPurger extends BukkitRunnable {
    private final QuickShop plugin;
    private volatile boolean executing;

    @Override
    public void run() {
        Util.ensureThread(true);
        if (executing) {
            plugin.getLogger().info("[Shop Purger] Another purge task still running!");
            return;
        }
        executing = true;
        if (!plugin.getConfiguration().getBoolean("purge.enabled")) {
            return;
        }
        Util.debugLog("[Shop Purger] Scanning and removing shops");
        int days = plugin.getConfiguration().getOrDefault("purge.days", 360);
        boolean deleteBanned = plugin.getConfiguration().getBoolean("purge.banned");
        boolean skipOp = plugin.getConfiguration().getBoolean("purge.skip-op");
        boolean returnCreationFee = plugin.getConfiguration().getBoolean("purge.return-create-fee");
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(shop.getOwner());
            if (!player.hasPlayedBefore()) {
                Util.debugLog("Shop " + shop + " detection skipped: Owner never played before.");
                continue;
            }
            long lastPlayed = player.getLastPlayed();
            if (lastPlayed == 0) {
                continue;
            }
            if (player.isOnline()) {
                continue;
            }
            if (player.isOp() && skipOp) {
                continue;
            }
            boolean markDeletion = player.isBanned() && deleteBanned;
            //noinspection ConstantConditions
            long noOfDaysBetween = ChronoUnit.DAYS.between(Util.getDateTimeFromTimestamp(lastPlayed), Util.getDateTimeFromTimestamp(System.currentTimeMillis()));
            if (noOfDaysBetween > days) {
                markDeletion = true;
            }
            if (!markDeletion) {
                continue;
            }
            plugin.getLogger().info("[Shop Purger] Shop " + shop + " has been purged.");
            shop.delete(false);
            if (returnCreationFee) {
                EconomyTransaction transaction =
                        EconomyTransaction.builder()
                                .amount(plugin.getConfiguration().getDouble("shop.cost"))
                                .allowLoan(false)
                                .core(QuickShop.getInstance().getEconomy())
                                .currency(shop.getCurrency())
                                .world(shop.getLocation().getWorld())
                                .to(shop.getOwner())
                                .build();
                transaction.failSafeCommit();
            }
            executing = false;
            plugin.getLogger().info("[Shop Purger] Task completed");
        }

    }
}
