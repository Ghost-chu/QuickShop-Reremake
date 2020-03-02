/*
 * This file is a part of project QuickShop, the name is SubCommand_Clean.java
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
import java.util.Iterator;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.ContainerShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;

public class SubCommand_Clean implements CommandProcesser {

    private final QuickShop plugin = QuickShop.instance;

    @Override
    public void onCommand(
        @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender instanceof Server) {
            sender.sendMessage("Can't run this command by Console");
            return;
        }

        sender.sendMessage(MsgUtil.getMessage("command.cleaning", sender));

        final Iterator<Shop> shIt = plugin.getShopManager().getShopIterator();
        final ArrayList<Shop> pendingRemoval = new java.util.ArrayList<>();
        int i = 0;

        while (shIt.hasNext()) {
            final Shop shop = shIt.next();

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
            shop.delete();
        }

        MsgUtil.clean();
        sender.sendMessage(MsgUtil.getMessage("command.cleaned", sender, "" + i));
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
        @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

}
