package org.maxgamer.quickshop.command.subcommand;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SubCommand_Transfer implements CommandProcesser {

    private final QuickShop plugin;

    public SubCommand_Transfer(QuickShop plugin) {
        this.plugin = plugin;
    }


    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length == 1) {
            if (!(sender instanceof Player)) {
                MsgUtil.sendMessage(sender, "Only player can run this command");
                return;
            }
            final OfflinePlayer targetPlayer = Bukkit.getServer().getOfflinePlayer(cmdArg[0]);
            String targetPlayerName = targetPlayer.getName();
            if (targetPlayerName == null) {
                targetPlayerName = "null";
            }
            final UUID targetPlayerUUID = targetPlayer.getUniqueId();
            for (Shop shop : plugin.getShopManager().getPlayerAllShops(((Player) sender).getUniqueId())) {
                shop.setOwner(targetPlayerUUID);
            }
            MsgUtil.sendMessage(sender, MsgUtil.getMessage("command.transfer-success", sender, sender.getName(), targetPlayerName));
        } else if (cmdArg.length == 2) {
            if (!QuickShop.getPermissionManager().hasPermission(sender, "quickshop.transfer.other")) {
                MsgUtil.sendMessage(sender, MsgUtil.getMessage("no-permission", sender));
                return;
            }
            final OfflinePlayer fromPlayer = Bukkit.getServer().getOfflinePlayer(cmdArg[0]);
            String fromPlayerName = fromPlayer.getName();
            if (fromPlayerName == null) {
                fromPlayerName = "null";
            }
            final OfflinePlayer targetPlayer = Bukkit.getServer().getOfflinePlayer(cmdArg[1]);
            String targetPlayerName = targetPlayer.getName();
            if (targetPlayerName == null) {
                targetPlayerName = "null";
            }
            final UUID targetPlayerUUID = targetPlayer.getUniqueId();
            for (Shop shop : plugin.getShopManager().getPlayerAllShops(fromPlayer.getUniqueId())) {
                shop.setOwner(targetPlayerUUID);
            }
            MsgUtil.sendMessage(sender, MsgUtil.getMessage("command.transfer-success-other", sender, fromPlayerName, targetPlayerName));

        } else MsgUtil.sendMessage(sender, MsgUtil.getMessage("command.wrong-args", sender));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return cmdArg.length <= 2 ? Util.getPlayerList() : Collections.emptyList();
    }
}
