package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.ContainerShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;
import sun.awt.OSInfo;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_SilentEmpty implements CommandProcesser {

    private final QuickShop plugin = QuickShop.instance;

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 4) {
            Util.debugLog("Exception on command, cancel.");
            return;
        }

        final Shop shop = plugin.getShopManager().getShop(new Location(plugin.getServer().getWorld(cmdArg[0]), Integer.parseInt(cmdArg[1]),
            Integer.parseInt(cmdArg[2]), Integer.parseInt(cmdArg[3])));

        if (!(shop instanceof ContainerShop)) {
            sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop", sender));
            return;
        }

        final ContainerShop cs = (ContainerShop) shop;
        final Inventory inventory = cs.getInventory();

        if (inventory == null) {
            // TODO: 24/11/2019 Send message about that issue.
            return;
        }

        inventory.clear();
        MsgUtil.sendControlPanelInfo(sender, shop);
        sender.sendMessage(MsgUtil.getMessage("empty-success", sender));
    }
}
