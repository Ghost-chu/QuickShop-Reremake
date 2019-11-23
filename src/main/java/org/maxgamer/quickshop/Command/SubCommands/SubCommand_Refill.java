package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_Refill implements CommandProcesser {

    private final QuickShop plugin = QuickShop.instance;

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        final ArrayList<String> list = new ArrayList<>();

        list.add(MsgUtil.getMessage("tabcomplete.amount", sender));

        return list;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Can't run by Console");
            return;
        }

        if (cmdArg.length < 1) {
            sender.sendMessage(MsgUtil.getMessage("command.no-amount-given", sender));
            return;
        }

        final int add;

        try {
            add = Integer.parseInt(cmdArg[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(MsgUtil.getMessage("thats-not-a-number", sender));
            return;
        }

        final BlockIterator bIt = new BlockIterator((LivingEntity) sender, 10);

        if (!bIt.hasNext()) {
            sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop", sender));
            return;
        }

        while (bIt.hasNext()) {
            final Block b = bIt.next();
            final Shop shop = plugin.getShopManager().getShop(b.getLocation());

            if (shop == null) {
                continue;
            }

            shop.add(shop.getItem(), add);
            sender.sendMessage(MsgUtil.getMessage("refill-success", sender));
            return;
        }

        sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop", sender));
    }
}
