package org.maxgamer.quickshop.Watcher;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Updater;

public class UpdateWatcher implements Listener {
	static BukkitTask cronTask = null;
	static boolean hasNewUpdate = false;

	public static void init() {
		cronTask = new BukkitRunnable() {

			@Override
			public void run() {
				if (!Updater.checkUpdate()) {
					hasNewUpdate = false;
					return;
				}
				QuickShop.instance.getLogger().info("New QuickShop release now updated on SpigotMC.org!");
				QuickShop.instance.getLogger().info("Update plugin in there: https://www.spigotmc.org/resources/62575/");
				hasNewUpdate = true;
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
					e.getPlayer().sendMessage(ChatColor.GREEN + "New QuickShop release now updated on SpigotMC.org!");
					e.getPlayer().sendMessage(
							ChatColor.GREEN + "Update plugin in there: https://www.spigotmc.org/resources/62575/");
				}

			}
		}.runTaskLaterAsynchronously(QuickShop.instance, 80);
	}

}
