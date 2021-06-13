/*
 * This file is a part of project QuickShop, the name is SubCommand_Size.java
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
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class SubCommand_Size implements CommandProcesser {
    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (!(sender instanceof Player)) {
            MsgUtil.sendDirectMessage(sender, "This command can't be run by the console!");
            return;
        }
        if (cmdArg.length < 1) {
            MsgUtil.sendMessage(sender, "command.bulk-size-not-set");
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(cmdArg[0]);
        } catch (NumberFormatException e) {
            MsgUtil.sendMessage(sender, "not-a-integer", cmdArg[0]);
            return;
        }
        final BlockIterator bIt = new BlockIterator((Player) sender, 10);
        // Loop through every block they're looking at upto 10 blocks away
        if (!bIt.hasNext()) {
            MsgUtil.sendMessage(sender, "not-looking-at-shop");
            return;
        }

        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                if (shop.getModerator().isModerator(((Player) sender).getUniqueId()) || sender.hasPermission("quickshop.other.amount")) {
                    if (amount <= 0 || amount > Util.getItemMaxStackSize(shop.getItem().getType())) {
                        MsgUtil.sendMessage(sender, "command.invalid-bulk-amount", Integer.toString(amount));
                        return;
                    }
                    shop.getItem().setAmount(amount);
                    shop.refresh();
                    MsgUtil.sendMessage(sender, "command.bulk-size-now", Integer.toString(shop.getItem().getAmount()), Util.getItemStackName(shop.getItem()));
                    return;
                } else {
                    MsgUtil.sendMessage(sender, "not-managed-shop");
                }
            }
        }
        MsgUtil.sendMessage(sender, "not-looking-at-shop");


    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length == 1 ? Collections.singletonList(MsgUtil.getMessage("tabcomplete.amount", sender)) : Collections.emptyList();
    }
}
