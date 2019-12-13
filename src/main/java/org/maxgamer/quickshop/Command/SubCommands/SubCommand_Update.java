/*
 * This file is a part of project QuickShop, the name is SubCommand_Update.java
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

package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.UpdateInfomation;
import org.maxgamer.quickshop.Util.Updater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SubCommand_Update implements CommandProcesser {

    private final QuickShop plugin = QuickShop.instance;

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        new BukkitRunnable() {
            @Override
            public void run() {
                sender.sendMessage(ChatColor.YELLOW + "Checking for updates...");

                final UpdateInfomation updateInfomation = Updater.checkUpdate();
                final String updateVersion = updateInfomation.getVersion();

                if (updateVersion == null) {
                    sender.sendMessage(ChatColor.RED + "Failed check the update, connection issue?");
                    return;
                }

                if (updateVersion.equals(plugin.getDescription().getVersion())) {
                    sender.sendMessage(ChatColor.GREEN + "No updates can update now.");
                    return;
                }

                sender.sendMessage(ChatColor.YELLOW + "Downloading update, this may need a while...");

                final byte[] pluginBin;

                try {
                    pluginBin = Updater.downloadUpdatedJar();
                } catch (IOException e) {
                    sender.sendMessage(ChatColor.RED + "Update failed, get details to look the console.");
                    plugin.getSentryErrorReporter().ignoreThrow();
                    e.printStackTrace();
                    return;
                }

                if (pluginBin.length < 1) {
                    sender.sendMessage(ChatColor.RED + "Download failed, check your connection before contact the author.");
                    return;
                }

                sender.sendMessage(ChatColor.YELLOW + "Installing update...");

                try {
                    Updater.replaceTheJar(pluginBin);
                } catch (IOException ioe) {
                    sender.sendMessage(ChatColor.RED + "Update failed, get details to look the console.");
                    plugin.getSentryErrorReporter().ignoreThrow();
                    ioe.printStackTrace();
                    return;
                } catch (RuntimeException re) {
                    sender.sendMessage(ChatColor.RED + "Update failed, " + re.getMessage());
                    return;
                }

                sender.sendMessage(ChatColor.GREEN + "Successfully, restart your server to apply the changes!");
            }
        }.runTaskAsynchronously(plugin);

    }

}
