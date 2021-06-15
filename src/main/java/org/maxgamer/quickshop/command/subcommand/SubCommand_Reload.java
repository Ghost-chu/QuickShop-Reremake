/*
 * This file is a part of project QuickShop, the name is SubCommand_Reload.java
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
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandHandler;
import org.maxgamer.quickshop.nonquickshopstuff.com.rylinaux.plugman.util.PluginUtil;
import org.maxgamer.quickshop.util.MsgUtil;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

@AllArgsConstructor
public class SubCommand_Reload implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        MsgUtil.sendMessage(sender, "server-crash-warning");
        MsgUtil.sendMessage(sender, "command.reloading");
        // Force save maps and players to prevent server crashing.
        Bukkit.savePlayers();
        Bukkit.getWorlds().forEach(World::save);

        PluginManager pluginManager = plugin.getServer().getPluginManager();
        try {
            File file = Paths.get(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toFile();
            Throwable throwable = PluginUtil.unload(plugin);
            if (throwable != null) {
                throw new IllegalStateException("Failed to reload QuickShop! Please consider restarting the server. (Plugin unloading has failed)", throwable);
            }
            Plugin plugin = pluginManager.loadPlugin(file);
            if (plugin != null) {
                plugin.onLoad();
                pluginManager.enablePlugin(plugin);
            } else {
                throw new IllegalStateException("Failed to reload QuickShop! Please consider restarting the server. (Plugin loading has failed)");
            }
        } catch (URISyntaxException | InvalidDescriptionException | InvalidPluginException e) {
            throw new RuntimeException("Failed to reload QuickShop! Please consider restarting the server.", e);
        }
    }


}
