/*
 * This file is a part of project QuickShop, the name is SubCommand_SetOwner.java
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

package org.maxgamer.quickshop.command.subcommand;

import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_SetOwner implements CommandProcesser {

    private final QuickShop plugin = QuickShop.instance;

    @Override
    public void onCommand(
        @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (!(sender instanceof Player)) {
            MsgUtil.sendMessage(sender,MsgUtil.getMessage("Only player can run this command", sender));
            return;
        }

        if (cmdArg.length < 1) {
            MsgUtil.sendMessage(sender,MsgUtil.getMessage("command.no-owner-given", sender));
            return;
        }

        final BlockIterator bIt = new BlockIterator((Player) sender, 10);

        if (!bIt.hasNext()) {
            MsgUtil.sendMessage(sender,MsgUtil.getMessage("not-looking-at-shop", sender));
            return;
        }

        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());

            if (shop == null) {
                continue;
            }

            @SuppressWarnings("deprecation") final OfflinePlayer newShopOwner = plugin.getServer().getOfflinePlayer(cmdArg[0]);
            if (newShopOwner.getName() == null) {
                MsgUtil.sendMessage(sender, MsgUtil.getMessage("unknown-player", null));
                return;
            }
            shop.setOwner(newShopOwner.getUniqueId());
            // shop.setSignText();
            shop.update();
            MsgUtil.sendMessage(sender, MsgUtil.getMessage("command.new-owner", sender, newShopOwner.getName()));
            return;
        }

        MsgUtil.sendMessage(sender,MsgUtil.getMessage("not-looking-at-shop", sender));
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
        @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

}
