/*
 * This file is a part of project QuickShop, the name is SubCommand_Item.java
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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

@AllArgsConstructor
public class SubCommand_Item implements CommandProcesser {
    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (!(sender instanceof Player)) {
            MsgUtil.sendMessage(sender, "This command can't be run by the console");
            return;
        }
        final BlockIterator bIt = new BlockIterator((Player) sender, 10);
        // Loop through every block they're looking at upto 10 blocks away
        if (!bIt.hasNext()) {
            MsgUtil.sendMessage(sender, MsgUtil.getMessage("not-looking-at-shop", sender));
            return;
        }
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());

            if (shop != null) {
                if (!shop.getModerator().isModerator(((Player) sender).getUniqueId()) && !QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.item")) {
                    MsgUtil.sendMessage(sender, MsgUtil.getMessage("not-managed-shop", sender));
                    return;
                }
                ItemStack itemStack = ((Player) sender).getInventory().getItemInMainHand().clone();
                if (itemStack.getType() == Material.AIR) {
                    MsgUtil.sendMessage(sender, MsgUtil.getMessage("command.no-trade-item", sender));
                    return;
                }
                if (Util.isBlacklisted(itemStack) && !QuickShop.getPermissionManager().hasPermission(sender, "quickshop.bypass." + itemStack.getType().name())) {
                    MsgUtil.sendMessage(sender, MsgUtil.getMessage("blacklisted-item", sender));
                    return;
                }
                if (!plugin.isAllowStack() && !QuickShop.getPermissionManager().hasPermission(sender, "quickshop.create.stacks")) {
                    itemStack.setAmount(1);
                }
                shop.setItem(itemStack);
                plugin.getQuickChat().send(sender, plugin.getQuickChat().getItemHologramChat(shop, shop.getItem(), (Player) sender, MsgUtil.getMessage("command.trade-item-now", sender, Integer.toString(shop.getItem().getAmount()), Util.getItemStackName(shop.getItem()))));
                return;
            }
            // shop.setSignText();
        }
        MsgUtil.sendMessage(sender, MsgUtil.getMessage("not-looking-at-shop", sender));
    }

}
