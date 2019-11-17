package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.ContainerShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class SubCommand_Price implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        ArrayList<String> list = new ArrayList<>();
        list.add(MsgUtil.getMessage("tabcomplete.price", sender));
        return list;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (cmdArg.length < 1) {
                sender.sendMessage(MsgUtil.getMessage("no-price-given", sender));
                return;
            }
            double price;
			double minPrice = plugin.getConfig().getDouble("shop.minimum-price");
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
                     //No number input
                     Util.debugLog(ex.getMessage());
                      p.sendMessage(MsgUtil.getMessage("not-a-number", p, cmdArg[0]));
                      return;
            }
	    boolean format = plugin.getConfig().getBoolean("use-deciaml-format");
            if (plugin.getConfig().getBoolean("shop.allow-free-shop")) {
                if (price != 0 && price < minPrice) {
                    p.sendMessage(MsgUtil.getMessage("price-too-cheap", p, (format) ? MsgUtil.decimalFormat(minPrice) : "" + minPrice));
                    return;
                }
            } else {
                if (price < minPrice) {
                    p.sendMessage(MsgUtil.getMessage("price-too-cheap", p, (format) ? MsgUtil.decimalFormat(minPrice) : "" + minPrice));
                    return;
                }
            }
            double price_limit = plugin.getConfig().getDouble("shop.maximum-price");
            if (price_limit != -1) {
                if (price > price_limit) {
                    p.sendMessage(MsgUtil.getMessage("price-too-high", p, (format) ? MsgUtil.decimalFormat(price_limit) : "" + price_limit));
                    return;
                }
            }
            double fee = 0;
            if (plugin.isPriceChangeRequiresFee()) {
                fee = plugin.getConfig().getDouble("shop.fee-for-price-change");
            }
            // if (fee > 0 && plugin.getEconomy().getBalance(p.getUniqueId()) < fee) {
            //     sender.sendMessage(
            //             MsgUtil.getMessage("you-cant-afford-to-change-price", plugin.getEconomy().format(fee)));
            //     return;
            // }
            BlockIterator bIt = new BlockIterator(p, 10);
            // Loop through every block they're looking at upto 10 blocks away
            if (!bIt.hasNext()) {
                sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop", sender));
                return;
            }
            while (bIt.hasNext()) {
                Block b = bIt.next();
                Shop shop = plugin.getShopManager().getShop(b.getLocation());
                if (shop != null && (shop.getModerator().isModerator(((Player) sender).getUniqueId()) || QuickShop.getPermissionManager().hasPermission(sender, "quickshop.other.price"))) {
                    if (shop.getPrice() == price) {
                        // Stop here if there isn't a price change
                        sender.sendMessage(MsgUtil.getMessage("no-price-change", sender));
                        return;
                    }
                    if (fee > 0) {
                        if (!plugin.getEconomy().withdraw(p.getUniqueId(), fee)) {
                            sender.sendMessage(MsgUtil.getMessage("you-cant-afford-to-change-price", sender,
                                    plugin.getEconomy().format(fee)));
                            return;
                        }
                        sender.sendMessage(
                                MsgUtil.getMessage("fee-charged-for-price-change", sender, plugin.getEconomy().format(fee)));
                        try {
                            plugin.getEconomy().deposit(Bukkit.getOfflinePlayer(plugin.getConfig().getString("tax-account"))
                                    .getUniqueId(), fee);
                        } catch (Exception e) {
                            e.getMessage();
                            plugin.getLogger().log(Level.WARNING,
                                    "QuickShop can't pay tax to the account in config.yml, please set the tax account name to an existing player!");
                        }

                    }
                    // Update the shop
                    shop.setPrice(price);
                    //shop.setSignText();
                    shop.update();
                    sender.sendMessage(MsgUtil.getMessage("price-is-now", sender, plugin.getEconomy().format(shop.getPrice())));
                    // Chest shops can be double shops.
                    if (shop instanceof ContainerShop) {
                        ContainerShop cs = (ContainerShop) shop;
                        if (cs.isDoubleShop()) {
                            Shop nextTo = cs.getAttachedShop();
                            if (cs.isSelling()) {
                                if (cs.getPrice() < nextTo.getPrice()) {
                                    sender.sendMessage(MsgUtil.getMessage("buying-more-than-selling", sender));
                                }
                            } else {
                                // Buying
                                if (cs.getPrice() > nextTo.getPrice()) {
                                    sender.sendMessage(MsgUtil.getMessage("buying-more-than-selling", sender));
                                }
                            }
                        }
                        return;
                    }
                    return;
                }
            }
            sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop", sender));
            return;
        }
        sender.sendMessage("Can't run this command by Console");
    }
}
