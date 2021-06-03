/*
 * This file is a part of project QuickShop, the name is SubCommand_Price.java
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
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.shop.ContainerShop;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

@AllArgsConstructor
public class SubCommand_Price implements CommandProcesser {

    private final QuickShop plugin;

    @Override
    public void onCommand(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (!(sender instanceof Player)) {
            MsgUtil.sendMessage(sender, "This command can't be run by the console!");
            return;
        }

        final Player p = (Player) sender;

        if (cmdArg.length < 1) {
            MsgUtil.sendMessage(sender, MsgUtil.getMessage("no-price-given", sender));
            return;
        }

        final double price;
        final double minPrice = plugin.getConfig().getDouble("shop.minimum-price");

        //TODO Migrate to PriceLimiter
        if (plugin.getConfig().getBoolean("whole-number-prices-only")) {
            try {
                price = Long.parseLong(cmdArg[0]);
            } catch (NumberFormatException ex2) {
                // input is number, but not Integer
                Util.debugLog(ex2.getMessage());
                MsgUtil.sendMessage(p, MsgUtil.getMessage("not-a-integer", p, cmdArg[0]));
                return;
            }
        } else {
            try {
                price = Double.parseDouble(cmdArg[0]);

            } catch (NumberFormatException ex) {
                // No number input
                Util.debugLog(ex.getMessage());
                MsgUtil.sendMessage(p, MsgUtil.getMessage("not-a-number", p, cmdArg[0]));
                return;
            }
            // No number input
            if (Double.isInfinite(price) || Double.isNaN(price)) {
                MsgUtil.sendMessage(p, MsgUtil.getMessage("not-a-number", p, cmdArg[0]));
                return;
            }
        }

        final boolean format = plugin.getConfig().getBoolean("use-decimal-format");

        if (plugin.getConfig().getBoolean("shop.allow-free-shop")) {
            if (price != 0 && price < minPrice) {
                MsgUtil.sendMessage(p,
                        MsgUtil.getMessage(
                                "price-too-cheap", p, (format) ? MsgUtil.decimalFormat(minPrice) : Double.toString(minPrice)));
                return;
            }
        } else {
            if (price < minPrice) {
                MsgUtil.sendMessage(p,
                        MsgUtil.getMessage(
                                "price-too-cheap", p, (format) ? MsgUtil.decimalFormat(minPrice) : Double.toString(minPrice)));
                return;
            }
        }

        final double price_limit = plugin.getConfig().getDouble("shop.maximum-price");

        if (price_limit != -1 && price > price_limit) {
            MsgUtil.sendMessage(p,
                    MsgUtil.getMessage(
                            "price-too-high",
                            p,
                            (format) ? MsgUtil.decimalFormat(price_limit) : Double.toString(price_limit)));
            return;
        }

        double fee = 0;

        if (plugin.isPriceChangeRequiresFee()) {
            fee = plugin.getConfig().getDouble("shop.fee-for-price-change");
        }

    /*if (fee > 0 && plugin.getEconomy().getBalance(p.getUniqueId()) < fee) {
        MsgUtil.sendMessage(sender,
            MsgUtil.getMessage("you-cant-afford-to-change-price", plugin.getEconomy().format(fee)));
        return;
    }*/
        final BlockIterator bIt = new BlockIterator(p, 10);
        // Loop through every block they're looking at upto 10 blocks away
        if (!bIt.hasNext()) {
            MsgUtil.sendMessage(sender, MsgUtil.getMessage("not-looking-at-shop", sender));
            return;
        }

        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());

            if (shop == null
                    || (!shop.getModerator().isModerator(((Player) sender).getUniqueId())
                    && !QuickShop.getPermissionManager()
                    .hasPermission(sender, "quickshop.other.price"))) {
                continue;
            }

            if (shop.getPrice() == price) {
                // Stop here if there isn't a price change
                MsgUtil.sendMessage(sender, MsgUtil.getMessage("no-price-change", sender));
                return;
            }

            if (fee > 0 && !plugin.getEconomy().withdraw(p.getUniqueId(), fee, shop.getLocation().getWorld(), shop.getCurrency())) {
                MsgUtil.sendMessage(sender,
                        MsgUtil.getMessage(
                                "you-cant-afford-to-change-price", sender, plugin.getEconomy().format(fee, shop.getLocation().getWorld(), shop.getCurrency())));
                return;
            }

            if (fee > 0) {
                MsgUtil.sendMessage(sender,
                        MsgUtil.getMessage(
                                "fee-charged-for-price-change", sender, plugin.getEconomy().format(fee, shop.getLocation().getWorld(), shop.getCurrency())));
                try {
                    //noinspection deprecation
                    plugin
                            .getEconomy()
                            .deposit(
                                    plugin
                                            .getServer()
                                            .getOfflinePlayer(
                                                    Objects.requireNonNull(plugin.getConfig().getString("tax-account")))
                                            .getUniqueId(),
                                    fee, shop.getLocation().getWorld(), shop.getCurrency());
                } catch (Exception e) {
                    e.getMessage();
                    plugin
                            .getLogger()
                            .log(
                                    Level.WARNING,
                                    "QuickShop can't pay taxes to the configured tax account! Please set the tax account name in the config.yml to an existing player!");
                }
            }
            // Update the shop
            shop.setPrice(price);
            // shop.setSignText();
            shop.update();
            MsgUtil.sendMessage(sender,
                    MsgUtil.getMessage("price-is-now", sender, plugin.getEconomy().format(shop.getPrice(), shop.getLocation().getWorld(), shop.getCurrency())));
            // Chest shops can be double shops.
            if (!(shop instanceof ContainerShop)) {
                return;
            }

            final ContainerShop cs = (ContainerShop) shop;

            if (!cs.isDoubleShop()) {
                return;
            }

            final Shop nextTo = cs.getAttachedShop();

            if (nextTo == null) {
                // TODO: 24/11/2019 Send message about that issue.
                return;
            }

            if (cs.isSelling()) {
                if (cs.getPrice() < nextTo.getPrice()) {
                    MsgUtil.sendMessage(sender, MsgUtil.getMessage("buying-more-than-selling", sender));
                }
            }
            // Buying
            else if (cs.getPrice() > nextTo.getPrice()) {
                MsgUtil.sendMessage(sender, MsgUtil.getMessage("buying-more-than-selling", sender));
            }

            return;
        }

        MsgUtil.sendMessage(sender, MsgUtil.getMessage("not-looking-at-shop", sender));
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length == 1 ? Collections.singletonList(MsgUtil.getMessage("tabcomplete.price", sender)) : Collections.emptyList();
    }

}
