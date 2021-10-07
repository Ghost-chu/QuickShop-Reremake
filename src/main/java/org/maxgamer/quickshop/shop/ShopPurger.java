package org.maxgamer.quickshop.shop;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.api.economy.EconomyTransaction;
import org.maxgamer.quickshop.util.Util;

import java.time.temporal.ChronoUnit;

@AllArgsConstructor
public class ShopPurger extends BukkitRunnable {
    private QuickShop plugin;
    private volatile boolean executing;

    @Override
    public void run() {
        Util.ensureThread(true);
        if (executing) {
            plugin.getLogger().info("[Shop Purger] Another purge task still running!");
            return;
        }
        executing = true;
        if (!plugin.getConfig().getBoolean("purge.enabled")) {
            return;
        }
        Util.debugLog("[Shop Purger] Scanning and removing shops");
        int days = plugin.getConfig().getInt("purge.days", 360);
        boolean deleteBanned = plugin.getConfig().getBoolean("purge.banned");
        boolean skipOp = plugin.getConfig().getBoolean("purge.skip-op");
        boolean returnCreationFee = plugin.getConfig().getBoolean("purge.return-create-fee");
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
                                .amount(plugin.getConfig().getDouble("shop.cost"))
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
