package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopType;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_Buy implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender instanceof Player) {
            BlockIterator bIt = new BlockIterator((LivingEntity) sender, 10);
            if (!bIt.hasNext()) {
                sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
                return;
            }
            while (bIt.hasNext()) {
                Block b = bIt.next();
                Shop shop = plugin.getShopManager().getShop(b.getLocation());
                if (shop != null && shop.getModerator().isModerator(((Player) sender).getUniqueId())) {
                    shop.setShopType(ShopType.BUYING);
                    //shop.setSignText();
                    shop.update();
                    sender.sendMessage(MsgUtil
                            .getMessage("command.now-buying", Util.getItemStackName(shop.getItem())));
                    return;
                }
            }
            sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
            return;
        }
        sender.sendMessage(MsgUtil.getMessage("Can't run command by Console"));
    }
}
