/*
 * This file is a part of project QuickShop, the name is UpdateWatcher.java
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

package org.maxgamer.quickshop.watcher;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.UpdateInfomation;
import org.maxgamer.quickshop.util.Updater;

import java.util.List;
import java.util.Random;

public class UpdateWatcher implements Listener {
    public static boolean hasNewUpdate = false;

    private static BukkitTask cronTask = null;

    private static UpdateInfomation info = null;

    public static String fixVer(@NotNull String originalVer) {
        originalVer = originalVer.replaceAll(QuickShop.getFork(), "");
        originalVer = originalVer.trim();
        return originalVer;
    }

    public static void init() {
        cronTask =
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        info = Updater.checkUpdate();

                        if (Updater.hasUpdate(info.getVersion())) {
                            hasNewUpdate = true;
                        } else {
                            return;
                        }

                        if (!info.isBeta()) {
                            QuickShop.instance
                                    .getLogger()
                                    .info(
                                            "A new version of QuickShop has been released! [" + info.getVersion() + "]");
                            QuickShop.instance
                                    .getLogger()
                            .info("Update here: https://www.spigotmc.org/resources/62575/");

                        Bukkit.getOnlinePlayers()
                            .forEach(
                                player -> {
                                    if (QuickShop.getPermissionManager()
                                        .hasPermission(player, "quickshop.alert")) {
                                        List<String> notifys =
                                            MsgUtil.getI18nFile().getStringList("updatenotify.list");
                                        Random random = new Random();
                                        int notifyNum = -1;
                                        if (notifys.size() > 1) {
                                            notifyNum = random.nextInt(notifys.size());
                                        }
                                        String notify;
                                        if (notifyNum > 0) { // Translate bug.
                                            notify = notifys.get(notifyNum);
                                        } else {
                                            notify = "New update {0} now avaliable! Please update!";
                                        }
                                        notify =
                                            MsgUtil.fillArgs(notify, info.getVersion(), QuickShop.getVersion());
                                        TextComponent updatenow =
                                            new TextComponent(
                                                ChatColor.AQUA
                                                    + MsgUtil.getMessage("updatenotify.buttontitle", player));
                                        TextComponent onekeyupdate =
                                            new TextComponent(
                                                ChatColor.YELLOW
                                                    + MsgUtil.getMessage(
                                                    "updatenotify.onekeybuttontitle", player));
                                        updatenow.setClickEvent(
                                            new ClickEvent(
                                                ClickEvent.Action.OPEN_URL,
                                                "https://www.spigotmc.org/resources/62575/"));
                                        onekeyupdate.setClickEvent(
                                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/qs update"));
                                        TextComponent finallyText =
                                            new TextComponent(updatenow, new TextComponent(" "), onekeyupdate);
                                        player.sendMessage(
                                            ChatColor.GREEN
                                                + "---------------------------------------------------");
                                        player.sendMessage(ChatColor.GREEN + notify);
                                        player.spigot().sendMessage(finallyText);
                                        player.sendMessage(
                                            ChatColor.GREEN
                                                + "---------------------------------------------------");
                                    }
                                });
                    } else {
                        QuickShop.instance.getLogger().info("A new BETA version of QuickShop is available!");
                        QuickShop.instance
                            .getLogger()
                            .info("Update here: https://www.spigotmc.org/resources/62575/");
                        QuickShop.instance
                            .getLogger()
                            .info("This is a BETA version, which means you should use it with caution.");
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
        if (!hasNewUpdate
            || !QuickShop.getPermissionManager().hasPermission(e.getPlayer(), "quickshop.alert")) {
            return;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!info.isBeta()) {
                    List<String> notifys = MsgUtil.getI18nFile().getStringList("updatenotify.list");
                    Random random = new Random();
                    int notifyNum = random.nextInt(notifys.size());
                    String notify = notifys.get(notifyNum);
                    notify = MsgUtil.fillArgs(notify, info.getVersion(), QuickShop.getVersion());

                    TextComponent updatenow =
                        new TextComponent(
                            ChatColor.AQUA + MsgUtil.getMessage("updatenotify.buttontitle", e.getPlayer()));
                    TextComponent onekeyupdate =
                        new TextComponent(
                            ChatColor.YELLOW
                                + MsgUtil.getMessage("updatenotify.onekeybuttontitle", e.getPlayer()));
                    updatenow.setClickEvent(
                        new ClickEvent(
                            ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/62575/"));
                    onekeyupdate.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/qs update"));
                    TextComponent finallyText =
                        new TextComponent(updatenow, new TextComponent(" "), onekeyupdate);
                    e.getPlayer()
                        .sendMessage(ChatColor.GREEN + "---------------------------------------------------");
                    e.getPlayer().sendMessage(ChatColor.GREEN + notify);
                    e.getPlayer().spigot().sendMessage(finallyText);
                    e.getPlayer()
                        .sendMessage(ChatColor.GREEN + "---------------------------------------------------");
                } else {
                    e.getPlayer()
                        .sendMessage(ChatColor.GRAY + "A new BETA version of QuickShop has been released!");
                    e.getPlayer()
                        .sendMessage(
                            ChatColor.GRAY + "Update here: https://www.spigotmc.org/resources/62575/");
                    e.getPlayer()
                        .sendMessage(
                            ChatColor.GRAY
                                + "This is a BETA version, which means you should use it with caution.");
                }
            }
        }.runTaskLater(QuickShop.instance, 80);
    }

}
