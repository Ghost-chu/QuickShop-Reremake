package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.ContainerShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SubCommand_Clean implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender instanceof Server) {
            sender.sendMessage("Can't run this command by Console");
            return;
        }
        sender.sendMessage(MsgUtil.getMessage("command.cleaning", sender));
        Iterator<Shop> shIt = plugin.getShopManager().getShopIterator();
        int i = 0;
        java.util.ArrayList<Shop> pendingRemoval = new java.util.ArrayList<>();

        while (shIt.hasNext()) {
            Shop shop = shIt.next();

            try {
                if (shop.getLocation().getWorld() != null && shop.isSelling() && shop.getRemainingStock() == 0
                        && shop instanceof ContainerShop) {
                    ContainerShop cs = (ContainerShop) shop;
                    if (cs.isDoubleShop()) {
                        continue;
                    }
                    pendingRemoval.add(shop); // Is selling, but has no stock, and is a chest shop, but is not a double shop.
                    // Can be deleted safely.
                    i++;
                }
            } catch (IllegalStateException e) {
                pendingRemoval.add(shop); // The shop is not there anymore, remove it
            }
        }

        for (Shop shop : pendingRemoval) {
            shop.delete();
        }

        MsgUtil.clean();
        sender.sendMessage(MsgUtil.getMessage("command.cleaned", sender, "" + i));
        return;

    }
}
