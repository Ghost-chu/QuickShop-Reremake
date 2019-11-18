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
                            Util.debugLog("Respawning the shop " + shop.toString() + " the display, cause it was despawned and a player close it");
                            shop.checkDisplay();
                        }
                    } else {
                        if (shop.getDisplay().isSpawned()) {
                            Util.debugLog("Removing the shop " + shop.toString() + " the display, cause nobody can see it");
                            shop.getDisplay().remove();
                        }
                    }
                }
            }.runTask(QuickShop.instance);
        });
    }
}
