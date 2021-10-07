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
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.api.shop.PriceLimiterCheckResult;
import org.maxgamer.quickshop.api.shop.PriceLimiterStatus;
import org.maxgamer.quickshop.economy.EconomyTransaction;
import org.maxgamer.quickshop.shop.ContainerShop;
import org.maxgamer.quickshop.api.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.shop.JavaPriceLimiter;
import org.maxgamer.quickshop.util.Util;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

@AllArgsConstructor
public class SubCommand_Price implements CommandHandler<Player> {

    private final QuickShop plugin;

    @Override
    public void onCommand(@NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            plugin.text().of(sender, "no-price-given").send();
            return;
        }

        final double price;

        try {
            price = Double.parseDouble(cmdArg[0]);
        } catch (NumberFormatException ex) {
            // No number input
            Util.debugLog(ex.getMessage());
            plugin.text().of(sender, "not-a-number", cmdArg[0]).send();
            return;
        }
        // No number input
        if (Double.isInfinite(price) || Double.isNaN(price)) {
            plugin.text().of(sender, "not-a-number", cmdArg[0]).send();
            return;
        }

        final boolean format = plugin.getConfig().getBoolean("use-decimal-format");

        double fee = 0;

        if (plugin.isPriceChangeRequiresFee()) {
            fee = plugin.getConfig().getDouble("shop.fee-for-price-change");
        }

        final BlockIterator bIt = new BlockIterator(sender, 10);
        // Loop through every block they're looking at upto 10 blocks away
        if (!bIt.hasNext()) {
            plugin.text().of(sender, "not-looking-at-shop").send();
            return;
        }

        JavaPriceLimiter limiter = new JavaPriceLimiter(
                plugin.getConfig().getDouble("shop.minimum-price"),
                plugin.getConfig().getInt("shop.maximum-price"),
                plugin.getConfig().getBoolean("shop.allow-free-shop"),
                plugin.getConfig().getBoolean("whole-number-prices-only"));

        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());

            if (shop == null
                    || (!shop.getModerator().isModerator(sender.getUniqueId())
                    && !QuickShop.getPermissionManager()
                    .hasPermission(sender, "quickshop.other.price"))) {
                continue;
            }

            if (shop.getPrice() == price) {
                // Stop here if there isn't a price change
                plugin.text().of(sender, "no-price-change").send();
                return;
            }

            PriceLimiterCheckResult checkResult = limiter.check(shop.getItem(), price);
            if (checkResult.getStatus() == PriceLimiterStatus.REACHED_PRICE_MIN_LIMIT) {
                plugin.text().of(sender, "price-too-cheap", (format) ? MsgUtil.decimalFormat(checkResult.getMin()) : Double.toString(checkResult.getMin())).send();
                return;
            }
            if (checkResult.getStatus() == PriceLimiterStatus.REACHED_PRICE_MAX_LIMIT) {
                plugin.text().of(sender, "price-too-high", (format) ? MsgUtil.decimalFormat(checkResult.getMax()) : Double.toString(checkResult.getMax())).send();
                return;
            }
            if (checkResult.getStatus() == PriceLimiterStatus.PRICE_RESTRICTED) {
                plugin.text().of(sender, "restricted-prices", Util.getItemStackName(shop.getItem()),
                        String.valueOf(checkResult.getMin()),
                        String.valueOf(checkResult.getMax())).send();
                return;
            }

            if (fee > 0) {
                EconomyTransaction transaction = EconomyTransaction.builder()
                        .allowLoan(plugin.getConfig().getBoolean("shop.allow-economy-loan", false))
                        .core(plugin.getEconomy())
                        .from(sender.getUniqueId())
                        .amount(fee)
                        .world(Objects.requireNonNull(shop.getLocation().getWorld()))
                        .currency(shop.getCurrency())
                        .build();
                if (!transaction.failSafeCommit()) {
                    EconomyTransaction.TransactionSteps steps = transaction.getSteps();
                    if (steps == EconomyTransaction.TransactionSteps.CHECK) {
                        plugin.text().of(sender,
                                "you-cant-afford-to-change-price", plugin.getEconomy().format(fee, shop.getLocation().getWorld(), shop.getCurrency())).send();
                    } else {
                        plugin.text().of(sender,
                                "fee-charged-for-price-change", plugin.getEconomy().format(fee, shop.getLocation().getWorld(), shop.getCurrency())).send();
                        plugin.getLogger().log(Level.WARNING, "QuickShop can't pay taxes to the configured tax account! Please set the tax account name in the config.yml to an existing player: " + transaction.getLastError());
                    }
                    return;
                }
            }
            // Update the shop
            shop.setPrice(price);
            shop.update();
            plugin.text().of(sender,
                    "price-is-now", plugin.getEconomy().format(shop.getPrice(), Objects.requireNonNull(shop.getLocation().getWorld()), shop.getCurrency())).send();
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
                    plugin.text().of(sender, "buying-more-than-selling").send();
                }
            }
            // Buying
            else if (cs.getPrice() > nextTo.getPrice()) {
                plugin.text().of(sender, "buying-more-than-selling").send();
            }

            return;
        }
        plugin.text().of(sender, "not-looking-at-shop").send();
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
            @NotNull Player sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length == 1 ? Collections.singletonList(QuickShop.getInstance().text().of(sender, "tabcomplete.price").forLocale()) : Collections.emptyList();
    }

}
