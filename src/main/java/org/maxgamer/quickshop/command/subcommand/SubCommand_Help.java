/*
 * This file is a part of project QuickShop, the name is SubCommand_Help.java
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
import org.maxgamer.quickshop.command.CommandContainer;
import org.maxgamer.quickshop.command.CommandHandler;
import org.maxgamer.quickshop.util.MsgUtil;

import java.util.List;

@AllArgsConstructor
public class SubCommand_Help implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        sendHelp(sender, commandLabel);
    }


    private void sendHelp(@NotNull CommandSender s, @NotNull String commandLabel) {
        MsgUtil.sendMessage(s, "command.description.title");
        commandCheckLoop:
        for (CommandContainer container : plugin.getCommandManager().getCmds()) {
            final List<String> requirePermissions = container.getPermissions();
            if (!container.isHidden() && requirePermissions != null && !requirePermissions.isEmpty()) {
                for (String requirePermission : requirePermissions) {
                    if (requirePermission != null && !QuickShop.getPermissionManager().hasPermission(s, requirePermission)) {
                        continue commandCheckLoop;
                    }
                }
                String commandDesc = MsgUtil.getMessage("command.description." + container.getPrefix(), s);
                if (container.getDescription() != null) {
                    commandDesc = container.getDescription();
                    if (commandDesc == null) {
                        commandDesc = "Error: Subcommand " + container.getPrefix() + " # " + container.getClass().getCanonicalName() + " doesn't register the correct help description.";
                    }
                }
                if (!container.isDisabled()) {
                    MsgUtil.sendDirectMessage(s,
                            ChatColor.GREEN //TODO: Color custom ability
                                    + "/"
                                    + commandLabel
                                    + " "
                                    + container.getPrefix()
                                    + ChatColor.YELLOW
                                    + " - "
                                    + commandDesc);
                } else if (QuickShop.getPermissionManager().hasPermission(s, "quickshop.showdisabled")) {
                    MsgUtil.sendDirectMessage(s,
                            ChatColor.RED
                                    + "/"
                                    + commandLabel
                                    + " "
                                    + container.getPrefix()
                                    + ChatColor.GRAY
                                    + " - "
                                    + MsgUtil.getMessage("command.disabled", s, ChatColor.GRAY + container.getDisableText(s)));

                }
            }
        }
    }

}
