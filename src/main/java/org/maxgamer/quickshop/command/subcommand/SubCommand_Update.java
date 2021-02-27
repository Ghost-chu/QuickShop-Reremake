/*
 * This file is a part of project QuickShop, the name is SubCommand_Update.java
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

package org.maxgamer.quickshop.command.subcommand;

import lombok.AllArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.util.MsgUtil;

import java.util.logging.Level;

@AllArgsConstructor
public class SubCommand_Update implements CommandProcesser {

    private final QuickShop plugin;

    @Override
    public void onCommand(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            MsgUtil.sendMessage(sender, ChatColor.YELLOW + "Checking for updates...");

            if (plugin.getUpdateWatcher() == null) {
                MsgUtil.sendMessage(sender, ChatColor.RED + "Updater seems has been disabled");
                return;
            }

            if (plugin.getUpdateWatcher().getUpdater().isLatest(plugin.getUpdateWatcher().getUpdater().getCurrentRunning())) {
                MsgUtil.sendMessage(sender, ChatColor.GREEN + "No updates can update now.");
                return;
            }

            MsgUtil.sendMessage(sender, ChatColor.YELLOW + "Downloading update, this may need a while...");

            //final byte[] pluginBin;

            try {
                plugin.getUpdateWatcher().getUpdater().install(plugin.getUpdateWatcher().getUpdater().update(plugin.getUpdateWatcher().getUpdater().getCurrentRunning()));
            } catch (Exception e) {
                MsgUtil.sendMessage(sender, ChatColor.RED + "Update failed, get details to look the console.");
                plugin.getSentryErrorReporter().ignoreThrow();
                plugin.getLogger().log(Level.WARNING, "Failed to update QuickShop cause something going wrong", e);
                return;
            }

//                MsgUtil.sendMessage(sender, ChatColor.YELLOW + "Installing update...");
//
//                try {
//                    Updater.replaceTheJar(pluginBin);
//                } catch (IOException ioe) {
//                    MsgUtil.sendMessage(sender, ChatColor.RED + "Update failed, get details to look the console.");
//                    plugin.getSentryErrorReporter().ignoreThrow();
//                    ioe.printStackTrace();
//                    return;
//                } catch (RuntimeException re) {
//                    MsgUtil.sendMessage(sender, ChatColor.RED + "Update failed, " + re.getMessage());
//                    return;
//                }

            MsgUtil.sendMessage(sender,
                    ChatColor.GREEN + "Successfully, restart your server to apply the changes!");
        });
    }

}
