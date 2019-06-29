package org.maxgamer.quickshop.Command.SubCommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;

public class SubCommand_SilentUnlimited implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender.hasPermission("quickshop.unlimited")) {

            Shop shop = plugin.getShopManager().getShop(new Location(Bukkit.getWorld(cmdArg[0]), Integer.valueOf(cmdArg[1]),
                    Integer.valueOf(cmdArg[2]), Integer.valueOf(cmdArg[3])));
            if (shop != null) {
                shop.setUnlimited(!shop.isUnlimited());
                //shop.setSignText();
                shop.update();
                MsgUtil.sendControlPanelInfo(sender, shop);
                sender.sendMessage(MsgUtil.getMessage("command.toggle-unlimited",
                        (shop.isUnlimited() ? "unlimited" : "limited")));
            }
        } else {
            sender.sendMessage(MsgUtil.getMessage("no-permission"));
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }
}
