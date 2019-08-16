package org.maxgamer.quickshop.Watcher;

import java.util.List;
import java.util.Random;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.UpdateInfomation;
import org.maxgamer.quickshop.Util.Updater;

public class UpdateWatcher implements Listener {
    private static BukkitTask cronTask = null;
    private static boolean hasNewUpdate = false;
    private static UpdateInfomation info = null;

    public static String fixVer(@NotNull String originalVer) {
        originalVer = originalVer.replaceAll("Reremake", "");
        originalVer = originalVer.trim();
        return originalVer;
    }

    public static void init() {
        cronTask = new BukkitRunnable() {

            @Override
            public void run() {
                info = Updater.checkUpdate();

                if (info.getVersion() == null) {
                    hasNewUpdate = false;
                    return;
                }

                if (info.getVersion().equals(QuickShop.getVersion())) {
                    hasNewUpdate = false;
                    return;
                }
                hasNewUpdate = true;

                if (!info.isBeta()) {
                    QuickShop.instance.getLogger().info("A new version of QuickShop has been released!");
                    QuickShop.instance.getLogger().info("Update here: https://www.spigotmc.org/resources/62575/");

                    Bukkit.getOnlinePlayers().forEach(player -> {
                        if (QuickShop.getPermissionManager().hasPermission(player,"quickshop.alert")) {
                            List<String> notifys = MsgUtil.getI18nYaml().getStringList("updatenotify.list");
                            Random random = new Random();
                            int notifyNum = -1;
                            if (notifys.size() > 1) {
                                notifyNum = random.nextInt(notifys.size());
                            }
                            String notify;
                            if (notifyNum > 0) { //Translate bug.
                                notify = notifys.get(notifyNum);
                            } else {
                                notify = "New update {0} now avaliable! Please update!";
                            }
                            notify = MsgUtil.fillArgs(notify, info.getVersion(), QuickShop.getVersion());
                            TextComponent updatenow = new TextComponent(ChatColor.AQUA + MsgUtil
                                    .getMessage("updatenotify.buttontitle"));
                            TextComponent onekeyupdate = new TextComponent(ChatColor.YELLOW + MsgUtil
                                    .getMessage("updatenotify.onekeybuttontitle"));
                            updatenow
                                    .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/62575/"));
                            onekeyupdate.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/qs update"));
                            TextComponent finallyText = new TextComponent(updatenow, new TextComponent(" "), onekeyupdate);
                            player.sendMessage(ChatColor.GREEN + "---------------------------------------------------");
                            player.sendMessage(ChatColor.GREEN + notify);
                            player.spigot().sendMessage(finallyText);
                            player.sendMessage(ChatColor.GREEN + "---------------------------------------------------");
                        }
                    });
                } else {
                    QuickShop.instance.getLogger().info("A new BETA version of QuickShop is available!");
                    QuickShop.instance.getLogger().info("Update here: https://www.spigotmc.org/resources/62575/");
                    QuickShop.instance.getLogger().info("This is a BETA version, which means you should use it with caution.");
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
                if (hasNewUpdate && QuickShop.getPermissionManager().hasPermission(e.getPlayer(),"quickshop.alert")) {
                    if (!info.isBeta()) {
                        List<String> notifys = MsgUtil.getI18nYaml().getStringList("updatenotify.list");
                        Random random = new Random();
                        int notifyNum = random.nextInt(notifys.size());
                        String notify = notifys.get(notifyNum);
                        notify = MsgUtil.fillArgs(notify, info.getVersion(), QuickShop.getVersion());

                        TextComponent updatenow = new TextComponent(ChatColor.AQUA + MsgUtil
                                .getMessage("updatenotify.buttontitle"));
                        TextComponent onekeyupdate = new TextComponent(ChatColor.YELLOW + MsgUtil
                                .getMessage("updatenotify.onekeybuttontitle"));
                        updatenow
                                .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/62575/"));
                        onekeyupdate.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/qs update"));
                        TextComponent finallyText = new TextComponent(updatenow, new TextComponent(" "), onekeyupdate);
                        e.getPlayer().sendMessage(ChatColor.GREEN + "---------------------------------------------------");
                        e.getPlayer().sendMessage(ChatColor.GREEN + notify);
                        e.getPlayer().spigot().sendMessage(finallyText);
                        e.getPlayer().sendMessage(ChatColor.GREEN + "---------------------------------------------------");
                    } else {
                        e.getPlayer().sendMessage(ChatColor.GRAY + "A new BETA version of QuickShop has been released!");
                        e.getPlayer().sendMessage(ChatColor.GRAY + "Update here: https://www.spigotmc.org/resources/62575/");
                        e.getPlayer()
                                .sendMessage(ChatColor.GRAY + "This is a BETA version, which means you should use it with caution.");
                    }
                }

            }
        }.runTaskLater(QuickShop.instance, 80);
    }

}
