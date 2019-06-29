package org.maxgamer.quickshop.Command.SubCommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.ContainerShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;

public class SubCommand_Empty implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender instanceof Player) {
            BlockIterator bIt = new BlockIterator((LivingEntity) sender, 10);
            if (!bIt.hasNext()) {
                sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
                return true;
            }
            while (bIt.hasNext()) {
                Block b = bIt.next();
                Shop shop = plugin.getShopManager().getShop(b.getLocation());
                if (shop != null) {
                    if (shop instanceof ContainerShop) {
                        ContainerShop cs = (ContainerShop) shop;
                        cs.getInventory().clear();
                        sender.sendMessage(MsgUtil.getMessage("empty-success"));
                        return true;
                    } else {
                        sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
                        return true;
                    }
                }
            }
        } else {
            sender.sendMessage("Can't run this command from Console");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }
}
