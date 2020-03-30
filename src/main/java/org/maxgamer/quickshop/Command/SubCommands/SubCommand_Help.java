/*
 * This file is a part of project QuickShop, the name is SubCommand_Help.java
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
import org.maxgamer.quickshop.Command.CommandContainer;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MsgUtil;

import java.util.Collections;
import java.util.List;

public class SubCommand_Help implements CommandProcesser {

    private final QuickShop plugin = QuickShop.instance;

    @Override
    public void onCommand(
        @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        sendHelp(sender, commandLabel);
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
        @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return Collections.emptyList();
    }

    private void sendHelp(@NotNull CommandSender s, @NotNull String commandLabel) {
        s.sendMessage(MsgUtil.getMessage("command.description.title", s));
        commandCheckLoop:
        for (CommandContainer container : plugin.getCommandManager().getCmds()) {
            final List<String> requirePermissions = container.getPermissions();
            if (!container.isHidden()&&requirePermissions != null && !requirePermissions.isEmpty()) {
                for (String requirePermission : requirePermissions) {
                    if (requirePermission!=null&&!QuickShop.getPermissionManager().hasPermission(s, requirePermission)) {
                        continue commandCheckLoop;
                    }
                }
                s.sendMessage(
                        ChatColor.GREEN
                                + "/"
                                + commandLabel
                                + " "
                                + container.getPrefix()
                                + ChatColor.YELLOW
                                + " - "
                                + MsgUtil.getMessage("command.description." + container.getPrefix(), s));
            }
        }
    }

}
