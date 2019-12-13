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

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;

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
        sender.sendMessage("[QuickShop] About QuickShop");
        sender.sendMessage("[QuickShop] Hello, I'm Ghost_chu Author of QS reremake.");
        sender.sendMessage("[QuickShop] This plugin is a remake by the SunnySide Community.");
        sender.sendMessage("[QuickShop] Original author is KaiNoMood. This is an unofficial QS version.");
        sender.sendMessage("[QuickShop] It has more feature, and has been designed for 1.13 and newer versions.");
        sender.sendMessage("[QuickShop] You can look at our SpigotMC page to learn more:");
        sender.sendMessage("[QuickShop] https://www.spigotmc.org/resources/62575/");
        sender.sendMessage("[QuickShop] Thanks for using QuickShop-Reremake.");
    }
}
