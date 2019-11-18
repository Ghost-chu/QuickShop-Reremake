package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_Unlimited implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender instanceof Player) {
            BlockIterator bIt = new BlockIterator((Player) sender, 10);
            if (!bIt.hasNext()) {
                sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop", sender));
                return;
            }
            while (bIt.hasNext()) {
                Block b = bIt.next();
                Shop shop = plugin.getShopManager().getShop(b.getLocation());
                if (shop != null) {
                    shop.setUnlimited(!shop.isUnlimited());
                    //shop.setSignText();
                    shop.update();
                    if (shop.isUnlimited()) {
                        sender.sendMessage(MsgUtil.getMessage("command.toggle-unlimited.unlimited", sender));
                    } else {
                        sender.sendMessage(MsgUtil.getMessage("command.toggle-unlimited.limited", sender));
                    }
                    return;
                }
            }
            sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop", sender));
        } else {
            sender.sendMessage("Only player can run this command.");
        }
    }
}
