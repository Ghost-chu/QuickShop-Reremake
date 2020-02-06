/*
 * This file is a part of project QuickShop, the name is SubCommand_Price.java
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
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.ContainerShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

public class SubCommand_Price implements CommandProcesser {

  private final QuickShop plugin = QuickShop.instance;

  @NotNull
  @Override
  public List<String> onTabComplete(
      @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
    ArrayList<String> list = new ArrayList<>();
    list.add(MsgUtil.getMessage("tabcomplete.price", sender));
    return list;
  }

  @Override
  public void onCommand(
      @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
    if (!(sender instanceof Player)) {
      sender.sendMessage("Can't run this command by Console");
      return;
    }

    final Player p = (Player) sender;

    if (cmdArg.length < 1) {
      sender.sendMessage(MsgUtil.getMessage("no-price-given", sender));
      return;
    }

    final double price;
    final double minPrice = plugin.getConfig().getDouble("shop.minimum-price");

    try {
      if (plugin.getConfig().getBoolean("whole-number-prices-only")) {
        try {
          price = Long.parseLong(cmdArg[0]);
        } catch (NumberFormatException ex2) {
          // input is number, but not Integer
          Util.debugLog(ex2.getMessage());
          p.sendMessage(MsgUtil.getMessage("not-a-integer", p, cmdArg[0]));
          return;
        }
      } else {
        price = Double.parseDouble(cmdArg[0]);
      }

    } catch (NumberFormatException ex) {
      // No number input
      Util.debugLog(ex.getMessage());
      p.sendMessage(MsgUtil.getMessage("not-a-number", p, cmdArg[0]));
      return;
    }

    final boolean format = plugin.getConfig().getBoolean("use-decimal-format");

    if (plugin.getConfig().getBoolean("shop.allow-free-shop")) {
      if (price != 0 && price < minPrice) {
        p.sendMessage(
            MsgUtil.getMessage(
                "price-too-cheap", p, (format) ? MsgUtil.decimalFormat(minPrice) : "" + minPrice));
        return;
      }
    } else {
      if (price < minPrice) {
        p.sendMessage(
            MsgUtil.getMessage(
                "price-too-cheap", p, (format) ? MsgUtil.decimalFormat(minPrice) : "" + minPrice));
        return;
      }
    }

    final double price_limit = plugin.getConfig().getDouble("shop.maximum-price");

    if (price_limit != -1 && price > price_limit) {
      p.sendMessage(
          MsgUtil.getMessage(
              "price-too-high",
              p,
              (format) ? MsgUtil.decimalFormat(price_limit) : "" + price_limit));
      return;
    }

    double fee = 0;

    if (plugin.isPriceChangeRequiresFee()) {
      fee = plugin.getConfig().getDouble("shop.fee-for-price-change");
    }

    /*if (fee > 0 && plugin.getEconomy().getBalance(p.getUniqueId()) < fee) {
        sender.sendMessage(
            MsgUtil.getMessage("you-cant-afford-to-change-price", plugin.getEconomy().format(fee)));
        return;
    }*/
    final BlockIterator bIt = new BlockIterator(p, 10);
    // Loop through every block they're looking at upto 10 blocks away
    if (!bIt.hasNext()) {
      sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop", sender));
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
        sender.sendMessage(MsgUtil.getMessage("no-price-change", sender));
        return;
      }

      if (fee > 0 && !plugin.getEconomy().withdraw(p.getUniqueId(), fee)) {
        sender.sendMessage(
            MsgUtil.getMessage(
                "you-cant-afford-to-change-price", sender, plugin.getEconomy().format(fee)));
        return;
      }

      if (fee > 0) {
        sender.sendMessage(
            MsgUtil.getMessage(
                "fee-charged-for-price-change", sender, plugin.getEconomy().format(fee)));
        try {
          plugin
              .getEconomy()
              .deposit(
                  plugin
                      .getServer()
                      .getOfflinePlayer(
                          Objects.requireNonNull(plugin.getConfig().getString("tax-account")))
                      .getUniqueId(),
                  fee);
        } catch (Exception e) {
          e.getMessage();
          plugin
              .getLogger()
              .log(
                  Level.WARNING,
                  "QuickShop can't pay tax to the account in config.yml, please set the tax account name to an existing player!");
        }
      }
      // Update the shop
      shop.setPrice(price);
      // shop.setSignText();
      shop.update();
      sender.sendMessage(
          MsgUtil.getMessage("price-is-now", sender, plugin.getEconomy().format(shop.getPrice())));
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
          sender.sendMessage(MsgUtil.getMessage("buying-more-than-selling", sender));
        }
      }
      // Buying
      else if (cs.getPrice() > nextTo.getPrice()) {
        sender.sendMessage(MsgUtil.getMessage("buying-more-than-selling", sender));
      }

      return;
    }

    sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop", sender));
  }
}
