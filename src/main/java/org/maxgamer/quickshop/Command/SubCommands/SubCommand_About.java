/*
 * This file is a part of project QuickShop, the name is SubCommand_About.java
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
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_About implements CommandProcesser {

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        sender.sendMessage(ChatColor.AQUA + "QuickShop " + ChatColor.YELLOW + QuickShop.instance.getFork());
        sender.sendMessage(ChatColor.AQUA + "Ver " + ChatColor.YELLOW + ">> " + ChatColor.GREEN + QuickShop.getVersion());
        if (QuickShop.getVersion().toUpperCase().contains("LTS")) {
            sender.sendMessage(ChatColor.AQUA + "Release " + ChatColor.YELLOW + ">> " + ChatColor.GREEN + MsgUtil.getMessage("updatenotify.label.lts", sender));
        } else if (QuickShop.getVersion().toUpperCase().contains("STABLE")) {
            sender.sendMessage(ChatColor.AQUA + "Release " + ChatColor.YELLOW + ">> " + ChatColor.GREEN + MsgUtil.getMessage("updatenotify.label.stable", sender));
        } else if (QuickShop.getVersion().toUpperCase().contains("QV")) {
            sender.sendMessage(ChatColor.AQUA + "Release " + ChatColor.YELLOW + ">> " + ChatColor.GREEN + MsgUtil.getMessage("updatenotify.label.qualityverifyed", sender));
        } else if (QuickShop.getVersion().toUpperCase().contains("BETA")) {
            sender.sendMessage(ChatColor.AQUA + "Release " + ChatColor.YELLOW + ">> " + ChatColor.GREEN + MsgUtil.getMessage("updatenotify.label.unstable", sender));
        } else if (QuickShop.getVersion().toUpperCase().contains("ALPHA")) {
            sender.sendMessage(ChatColor.AQUA + "Release " + ChatColor.YELLOW + ">> " + ChatColor.GREEN + MsgUtil.getMessage("updatenotify.label.unstable", sender));
        } else if (QuickShop.getVersion().toUpperCase().contains("EARLY ACCESS")) {
            sender.sendMessage(ChatColor.AQUA + "Release " + ChatColor.YELLOW + ">> " + ChatColor.GREEN + MsgUtil.getMessage("updatenotify.label.unstable", sender));
        } else if (QuickShop.getVersion().toUpperCase().contains("SNAPSHOT")) {
            sender.sendMessage(ChatColor.AQUA + "Release " + ChatColor.YELLOW + ">> " + ChatColor.GREEN + MsgUtil.getMessage("updatenotify.label.unstable", sender));
        } else {
            sender.sendMessage(ChatColor.AQUA + "Release " + ChatColor.YELLOW + ">> " + ChatColor.GREEN + "[Main Line]");
        }
        sender.sendMessage(ChatColor.AQUA + "Dev " + ChatColor.YELLOW + ">> " + ChatColor.GREEN + Util.list2String(QuickShop.instance.getDescription().getAuthors()));
        sender.sendMessage(ChatColor.GOLD + "Powered by Bukkit Common Studio");
        sender.sendMessage(ChatColor.RED + "Made with ❤");

    }
}
