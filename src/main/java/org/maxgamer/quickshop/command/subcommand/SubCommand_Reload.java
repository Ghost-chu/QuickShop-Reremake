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
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.util.MsgUtil;

import java.io.File;

@AllArgsConstructor
public class SubCommand_Reload implements CommandProcesser {

    private final QuickShop plugin;

    @Override
    public void onCommand(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        MsgUtil.sendMessage(sender, MsgUtil.getMessage("command.reloading", sender));
        plugin.getServer().getPluginManager().disablePlugin(plugin);
        try {
            Plugin newPlugin = Bukkit.getPluginManager().loadPlugin(new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getFile()));
            if (newPlugin == null) {
                throw new InvalidPluginException("Plugin loading failed - null instance returned.");
            }
            Bukkit.getPluginManager().enablePlugin(newPlugin);
        } catch (InvalidPluginException | InvalidDescriptionException e) {
            throw new RuntimeException("Failed to reload plugin, considering restart the server.", e);
        }
        //plugin.getServer().getPluginManager().enablePlugin(plugin);
    }


}
