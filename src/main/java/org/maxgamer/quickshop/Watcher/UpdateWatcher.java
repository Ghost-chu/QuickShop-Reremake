package org.maxgamer.quickshop.Watcher;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.UpdateInfomation;
import org.maxgamer.quickshop.Util.Updater;

public class UpdateWatcher implements Listener {
    static BukkitTask cronTask = null;
    static volatile boolean hasNewUpdate = false;
    static boolean isBeta = false;
    static UpdateInfomation info = null;

    public static void init() {
        cronTask = new BukkitRunnable() {

            @Override
            public void run() {
                info = Updater.checkUpdate();
                if (!info.getIsNewUpdate()) {
                    hasNewUpdate = false;
                    return;
                }

                hasNewUpdate = true;

                if (!info.getIsBeta()) {
                    QuickShop.instance.getLogger().warning("A new version of QuickShop has been released!");
                    QuickShop.instance.getLogger().warning("Update here: https://www.spigotmc.org/resources/62575/");

                    Bukkit.getOnlinePlayers().forEach(player -> {
                        if (player.isOp()) {
                            player.sendMessage(ChatColor.GREEN + "---------------------------------------------------");
                            player.sendMessage(ChatColor.GREEN + "A new version of QuickShop has been released!");
                            player.sendMessage(ChatColor.GREEN + "Update here: https://www.spigotmc.org/resources/62575/");
                            player.sendMessage(ChatColor.GREEN + "---------------------------------------------------");
                        }
                    });
                } else {
                    QuickShop.instance.getLogger().warning("A new BETA version of QuickShop is available!");
                    QuickShop.instance.getLogger().warning("Update here: https://www.spigotmc.org/resources/62575/");
                    QuickShop.instance.getLogger().warning("This is a BETA version, which means you should use with caution.");
                }

            }
        }.runTaskTimerAsynchronously(QuickShop.instance, 1, 20 * 60 * 60);
    }

    public static void uninit() {
        hasNewUpdate = false;
        if (cronTask == null) {
            return;
        }
        cronTask.cancel();
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (hasNewUpdate && e.getPlayer().hasPermission("quickshop.alert")) {
                    if (!info.getIsBeta()) {
                        e.getPlayer().sendMessage(ChatColor.GREEN + "A new version of QuickShop has been released!");
                        e.getPlayer().sendMessage(ChatColor.GREEN + "Update here: https://www.spigotmc.org/resources/62575/");
                    } else {
                        e.getPlayer().sendMessage(ChatColor.GRAY + "A new BETA version of QuickShop has been released!");
                        e.getPlayer().sendMessage(ChatColor.GRAY + "Update here: https://www.spigotmc.org/resources/62575/");
                        e.getPlayer()
                                .sendMessage(ChatColor.GRAY + "This is a BETA version, which means you should use with caution.");
                    }
                }

            }
        }.runTaskLater(QuickShop.instance, 80);
    }

}
