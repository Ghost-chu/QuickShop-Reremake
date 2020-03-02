/*
 * This file is a part of project QuickShop, the name is SubCommand_SilentUnlimited.java
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

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

public class SubCommand_SilentUnlimited implements CommandProcesser {
    private final QuickShop plugin = QuickShop.instance;

    @Override
    public void onCommand(
        @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 4) {
            Util.debugLog("Exception on command, cancel.");
            return;
        }

        final Shop shop =
            plugin
                .getShopManager()
                .getShop(
                    new Location(
                        plugin.getServer().getWorld(cmdArg[0]),
                        Integer.parseInt(cmdArg[1]),
                        Integer.parseInt(cmdArg[2]),
                        Integer.parseInt(cmdArg[3])));

        if (shop == null) {
            sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop", sender));
            return;
        }

        shop.setUnlimited(!shop.isUnlimited());
        // shop.setSignText();
        shop.update();
        MsgUtil.sendControlPanelInfo(sender, shop);

        if (shop.isUnlimited()) {
            sender.sendMessage(MsgUtil.getMessage("command.toggle-unlimited.unlimited", sender));
            return;
        }

        sender.sendMessage(MsgUtil.getMessage("command.toggle-unlimited.limited", sender));
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
        @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

}
