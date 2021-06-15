/*
 * This file is a part of project QuickShop, the name is SubCommand_Clean.java
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
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandHandler;
import org.maxgamer.quickshop.shop.ContainerShop;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class SubCommand_Clean implements CommandHandler<CommandSender> {

    private final QuickShop plugin;

    @Override
    public void onCommand(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        MsgUtil.sendMessage(sender, "command.cleaning");

        final List<Shop> pendingRemoval = new ArrayList<>();
        int i = 0;

        for (Shop shop : plugin.getShopManager().getAllShops()) {
            try {
                if (shop.getLocation().getWorld() != null
                        && shop.isSelling()
                        && shop.getRemainingStock() == 0
                        && shop instanceof ContainerShop) {
                    ContainerShop cs = (ContainerShop) shop;
                    if (cs.isDoubleShop()) {
                        continue;
                    }
                    pendingRemoval.add(
                            shop); // Is selling, but has no stock, and is a chest shop, but is not a double shop.
                    // Can be deleted safely.
                    i++;
                }
            } catch (IllegalStateException e) {
                pendingRemoval.add(shop); // The shop is not there anymore, remove it
            }
        }

        for (Shop shop : pendingRemoval) {
            plugin.log("Deleting shop " + shop + " as requested by the /qs clean command.");
            shop.delete();
        }

        MsgUtil.clean();
        MsgUtil.sendMessage(sender, "command.cleaned", Integer.toString(i));
    }


}
