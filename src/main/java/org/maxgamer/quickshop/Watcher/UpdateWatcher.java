package org.maxgamer.quickshop.Watcher;

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
	static boolean hasNewUpdate = false;
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
				
				if(!info.getIsBeta()) {
					QuickShop.instance.getLogger().info("New QuickShop released, now updated on SpigotMC.org!");
					QuickShop.instance.getLogger().info("Update here: https://www.spigotmc.org/resources/62575/");
					hasNewUpdate = true;
				}else {
					QuickShop.instance.getLogger().info("New QuickShop BETA released, you can updated on SpigotMC.org!");
					QuickShop.instance.getLogger().info("Update here: https://www.spigotmc.org/resources/62575/");
					QuickShop.instance.getLogger().info("This is a BETA version, that mean you shouldn't use it in Production Environment");
					hasNewUpdate = true;
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
	public void PlayerJoin(PlayerJoinEvent e) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (e.getPlayer().hasPermission("quickshop.alert") && hasNewUpdate) {
					if(!info.getIsBeta()) {
						e.getPlayer().sendMessage(ChatColor.GREEN+"New QuickShop released, now updated on SpigotMC.org!");
						e.getPlayer().sendMessage(ChatColor.GREEN+"Update here: https://www.spigotmc.org/resources/62575/");
					}else {
						e.getPlayer().sendMessage(ChatColor.GRAY+"New QuickShop BETA released, you can updated on SpigotMC.org!");
						e.getPlayer().sendMessage(ChatColor.GRAY+"Update here: https://www.spigotmc.org/resources/62575/");
						e.getPlayer().sendMessage(ChatColor.GRAY+"This is a BETA version, that mean you shouldn't use it in Production Environment");
					}
				}

			}
		}.runTaskLaterAsynchronously(QuickShop.instance, 80);
	}

}
