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
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.nonquickshopstuff.com.rylinaux.plugman.util.PluginUtil;
import org.maxgamer.quickshop.util.MsgUtil;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

@AllArgsConstructor
public class SubCommand_Reload implements CommandProcesser {

    private final QuickShop plugin;

    @Override
    public void onCommand(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        MsgUtil.sendMessage(sender, MsgUtil.getMessage("command.reloading", sender));
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        try {
            File file = Paths.get(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toFile();
            Throwable throwable = PluginUtil.unload(plugin);
            if (throwable != null) {
                throw new RuntimeException("Failed to reload plugin, considering restart the server. (Unload plugin failed)", throwable);
            }
            Plugin plugin = pluginManager.loadPlugin(file);
            if (plugin != null) {
                plugin.onLoad();
                pluginManager.enablePlugin(plugin);
            } else {
                throw new RuntimeException("Failed to reload plugin, considering restart the server. (Load plugin failed)");
            }
        } catch (URISyntaxException | InvalidDescriptionException | InvalidPluginException e) {
            throw new RuntimeException("Failed to reload plugin, considering restart the server.", e);
        }
    }


}
