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
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.PriceLimiter;
import org.maxgamer.quickshop.util.Util;

import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class SubCommand_Size implements CommandHandler<Player> {
    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            plugin.text().of(sender, "command.bulk-size-not-set").send();
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(cmdArg[0]);
        } catch (NumberFormatException e) {
            plugin.text().of(sender, "not-a-integer", cmdArg[0]).send();
            return;
        }
        final BlockIterator bIt = new BlockIterator(sender, 10);
        // Loop through every block they're looking at upto 10 blocks away
        if (!bIt.hasNext()) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }
        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());
            if (shop != null) {
                if (shop.getModerator().isModerator(sender.getUniqueId()) || sender.hasPermission("quickshop.other.amount")) {
                    if (amount <= 0 || amount > Util.getItemMaxStackSize(shop.getItem().getType())) {
                        plugin.text().of(sender, "command.invalid-bulk-amount", Integer.toString(amount)).send();
                        return;
                    }
                    ItemStack pendingItemStack = shop.getItem().clone();
                    pendingItemStack.setAmount(amount);
                    PriceLimiter limiter = new PriceLimiter(
                            plugin.getConfig().getDouble("shop.minimum-price"),
                            plugin.getConfig().getInt("shop.maximum-price"),
                            plugin.getConfig().getBoolean("shop.allow-free-shop"),
                            plugin.getConfig().getBoolean("whole-number-prices-only"));
                    PriceLimiter.CheckResult checkResult = limiter.check(pendingItemStack, shop.getPrice());
                    if (checkResult.getStatus() != PriceLimiter.Status.PASS) {
                        plugin.text().of(sender, "restricted-prices", Util.getItemStackName(shop.getItem()),
                                String.valueOf(checkResult.getMin()),
                                String.valueOf(checkResult.getMax())).send();
                        return;
                    }
                    shop.setItem(pendingItemStack);
                    plugin.text().of(sender, "command.bulk-size-now", Integer.toString(shop.getItem().getAmount()), Util.getItemStackName(shop.getItem())).send();
                    return;
                } else {
                    plugin.text().of(sender, "not-managed-shop").send();
                }
            }
        }
        plugin.text().of(sender, "not-looking-at-shop").send();


    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length == 1 ? Collections.singletonList(QuickShop.getInstance().text().of(sender, "tabcomplete.amount").forLocale()) : Collections.emptyList();
    }
}
