package org.maxgamer.quickshop.command.subcommand;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

public class SubCommand_Item implements CommandProcesser {
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (!(sender instanceof Player)) {
            MsgUtil.sendMessage(sender, "Can't run command by Console");
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
            final Shop shop = QuickShop.getInstance().getShopManager().getShop(b.getLocation());

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
                if (!QuickShop.getInstance().isAllowStack() && !QuickShop.getPermissionManager().hasPermission(sender, "quickshop.create.stacks")) {
                    itemStack.setAmount(1);
                }
                shop.setItem(itemStack);
                MsgUtil.sendItemholochat(shop, shop.getItem(), (Player) sender, MsgUtil.getMessage("command.trade-item-now", sender, Integer.toString(shop.getItem().getAmount()), Util.getItemStackName(shop.getItem())));
            }
            // shop.setSignText();
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return Collections.emptyList();
    }
}
