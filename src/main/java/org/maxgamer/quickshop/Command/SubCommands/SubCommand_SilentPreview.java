package org.maxgamer.quickshop.Command.SubCommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.ContainerShop;
import org.maxgamer.quickshop.Shop.InventoryPreview;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

public class SubCommand_SilentPreview implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender instanceof Player) {
            if (cmdArg.length < 4) {
                Util.debugLog("Exception on command, cancel.");
                return;
            }
            Shop shop = plugin.getShopManager().getShop(new Location(Bukkit.getWorld(cmdArg[0]),
                    Integer.parseInt(cmdArg[1]), Integer.parseInt(cmdArg[2]), Integer.parseInt(cmdArg[3])));
            if (shop != null) {
                if (shop instanceof ContainerShop) {
                    ContainerShop cs = (ContainerShop) shop;
                    InventoryPreview inventoryPreview = new InventoryPreview(cs.getItem(), (Player) sender);
                    inventoryPreview.show();
                    return;
                }
            }
            sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
        } else {
            sender.sendMessage("Can't run this command from Console");
        }
    }
}
