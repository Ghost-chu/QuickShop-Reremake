package org.maxgamer.quickshop.command.subcommand;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;

import java.util.ArrayList;
import java.util.List;

import static org.maxgamer.quickshop.util.Util.getPlayerList;

public class SubCommand_RemoveAll implements CommandProcesser {

    private static QuickShop plugin = QuickShop.instance;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length == 1) {
            //copy it first
            List<Shop> tempList = new ArrayList<>(plugin.getShopManager().getAllShops());
            OfflinePlayer shopOwner = null;
            for (OfflinePlayer player : plugin.getServer().getOfflinePlayers()) {
                if (player.getName() != null && player.getName().equalsIgnoreCase(cmdArg[0])) {
                    shopOwner = player;
                    break;
                }
            }
            if (shopOwner == null) {
                MsgUtil.sendMessage(sender, MsgUtil.getMessage("unknown-player", null));
                return;
            }
            int i = 0;
            for (Shop shop : tempList) {
                if (shop.getOwner().equals(shopOwner.getUniqueId())) {
                    shop.delete();
                    i++;
                }
            }

            MsgUtil.sendMessage(sender, MsgUtil.getMessage("command.some-shops-removed", sender, Integer.toString(i)));
        } else {
            MsgUtil.sendMessage(sender, MsgUtil.getMessage("command.no-owner-given", sender));
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return getPlayerList(cmdArg);
    }
}
