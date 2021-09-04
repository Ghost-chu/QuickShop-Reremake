/*
 * This file is a part of project QuickShop, the name is SubCommand_RemoveWorld.java
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
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandHandler;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

@AllArgsConstructor
public class SubCommand_RemoveWorld implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            MsgUtil.sendMessage(sender, "command.no-world-given");
            return;
        }
        World world = Bukkit.getWorld(cmdArg[0]);
        if (world == null) {
            MsgUtil.sendMessage(sender, "world-not-exists", cmdArg[0]);
            return;
        }
        int shopsDeleted = 0;
        for (Shop shop : plugin.getShopManager().getAllShops()) {
            if (shop.getLocation().getWorld().equals(world)) {
                shop.delete();
                shopsDeleted++;
            }
        }
        Util.debugLog("Successfully deleted all shops in world {0}!", cmdArg[0]);

        MsgUtil.sendMessage(sender, "shops-removed-in-world", String.valueOf(shopsDeleted), world.getName());
    }

}
