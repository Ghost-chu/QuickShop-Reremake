/*
 * This file is a part of project QuickShop, the name is PlayerOnlyCommandProcessor.java
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

package org.maxgamer.quickshop.command.processor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.util.MsgUtil;

public abstract class PlayerOnlyCommandProcessor implements ICommandProcessor {
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender instanceof Player) {
            onCommand((Player) sender, commandLabel, cmdArg);
        } else {
            MsgUtil.sendDirectMessage(sender, "This command is player only!");
        }
    }

    public abstract void onCommand(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] cmdArg);
}
