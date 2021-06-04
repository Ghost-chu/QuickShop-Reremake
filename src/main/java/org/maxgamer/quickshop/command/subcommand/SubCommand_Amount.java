/*
 * This file is a part of project QuickShop, the name is SubCommand_Amount.java
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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.util.MsgUtil;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class SubCommand_Amount implements CommandProcesser {

    private final QuickShop plugin;

    @Override
    public void onCommand(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            MsgUtil.sendMessage(sender, MsgUtil.getMessage("command.wrong-args", sender));
            return;
        }

        if (!(sender instanceof Player)) {
            MsgUtil.sendMessage(sender, "This command can't be run by the console!");
            return;
        }

        final Player player = (Player) sender;

        if (!plugin.getShopManager().getActions().containsKey(player.getUniqueId())) {
            MsgUtil.sendMessage(sender, MsgUtil.getMessage("no-pending-action", sender));
            return;
        }

        plugin.getShopManager().handleChat(player, cmdArg[0]);
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length == 1 ? Collections.singletonList(MsgUtil.getMessage("tabcomplete.amount", sender)) : Collections.emptyList();
    }

}
