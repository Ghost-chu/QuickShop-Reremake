package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.ContainerShop;
import org.maxgamer.quickshop.Shop.InventoryPreview;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_SilentPreview implements CommandProcesser {

    private final QuickShop plugin = QuickShop.instance;

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Can't run this command from Console");
            return;
        }

        if (cmdArg.length < 4) {
            Util.debugLog("Exception on command, cancel.");
            return;
        }

        final Shop shop = plugin.getShopManager().getShop(new Location(plugin.getServer().getWorld(cmdArg[0]),
            Integer.parseInt(cmdArg[1]), Integer.parseInt(cmdArg[2]), Integer.parseInt(cmdArg[3])));

        if (!(shop instanceof ContainerShop)) {
            sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop", sender));
            return;
        }

        new InventoryPreview(shop.getItem(), (Player) sender).show();
    }

}
