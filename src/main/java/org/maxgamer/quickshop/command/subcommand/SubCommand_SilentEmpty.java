/*
 * This file is a part of project QuickShop, the name is SubCommand_SilentEmpty.java
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
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.shop.ContainerShop;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

import java.util.UUID;

@AllArgsConstructor
public class SubCommand_SilentEmpty implements CommandProcesser {

    private final QuickShop plugin;

    @Override
    public void onCommand(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            Util.debugLog("Exception on command, cancel.");
            return;
        }

        Shop shop = plugin.getShopManager().getShopFromRuntimeRandomUniqueId(UUID.fromString(cmdArg[0]));
//        if (cmdArg.length < 1) {
//            Util.debugLog("Exception on command, cancel.");
//            return;
//        }
//
//        final Shop shop =
//                plugin
//                        .getShopManager()
//                        .getShop(
//                                new Location(
//                                        plugin.getServer().getWorld(cmdArg[0]),
//                                        Integer.parseInt(cmdArg[1]),
//                                        Integer.parseInt(cmdArg[2]),
//                                        Integer.parseInt(cmdArg[3])));

        if (!(shop instanceof ContainerShop)) {
            MsgUtil.sendMessage(sender, MsgUtil.getMessage("not-looking-at-shop", sender));
            return;
        }

        final ContainerShop cs = (ContainerShop) shop;
        final Inventory inventory = cs.getInventory();

        if (inventory == null) {
            Util.debugLog("Inventory is empty! " + cs);
            return;
        }

        inventory.clear();
        MsgUtil.sendControlPanelInfo(sender, shop);
        MsgUtil.sendMessage(sender, MsgUtil.getMessage("empty-success", sender));
    }

}
