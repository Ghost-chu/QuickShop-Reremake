package org.maxgamer.quickshop.Command.SubCommands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

public class SubCommand_Staff implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        ArrayList<String> tabList = new ArrayList<>();
        Util.debugLog(Util.array2String(cmdArg));
        if (cmdArg.length < 2) {
            if (cmdArg.length == 1) {
                String prefix = cmdArg[0].toLowerCase();
                if ("add".startsWith(prefix) || "add".equals(prefix))
                    tabList.add("add");
                if ("del".startsWith(prefix) || "del".equals(prefix))
                    tabList.add("del");
                if ("list".startsWith(prefix) || "list".equals(prefix))
                    tabList.add("list");
                if ("clear".startsWith(prefix) || "clear".equals(prefix))
                    tabList.add("clear");
            } else {
                tabList.add("add");
                tabList.add("del");
                tabList.add("list");
                tabList.add("clear");
            }

            return tabList;
        }
        if (cmdArg[0].equals("add") || cmdArg[0].equals("del")) {
            if (plugin.getConfig().getBoolean("include-offlineplayer-list")) {
                //Include
                for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                    tabList.add(offlinePlayer.getName());
                }
            } else {
                //Not Include
                for (OfflinePlayer offlinePlayer : Bukkit.getOnlinePlayers()) {
                    tabList.add(offlinePlayer.getName());
                }
            }
        }
        return tabList;
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        Util.debugLog(Util.array2String(cmdArg));
        if (sender instanceof Player) {
            BlockIterator bIt = new BlockIterator((LivingEntity) sender, 10);
            if (!bIt.hasNext()) {
                sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
                return;
            }
            while (bIt.hasNext()) {
                Block b = bIt.next();
                Shop shop = plugin.getShopManager().getShop(b.getLocation());
                if (shop != null && shop.getModerator().isModerator(((Player) sender).getUniqueId())) {
                    switch (cmdArg.length) {
                        case 0:
                            sender.sendMessage(MsgUtil.getMessage("command.wrong-args"));
                            return;
                        case 1:
                            switch (cmdArg[0]) {
                                case "add":
                                    sender.sendMessage(MsgUtil.getMessage("command.wrong-args"));
                                    return;
                                case "del":
                                    sender.sendMessage(MsgUtil.getMessage("command.wrong-args"));
                                    return;
                                case "clear":
                                    shop.clearStaffs();
                                    sender.sendMessage(MsgUtil.getMessage("shop-staff-cleared"));
                                    return;
                                case "list":
                                    for (UUID uuid : shop.getStaffs()) {
                                        sender.sendMessage(ChatColor.GREEN + MsgUtil.getMessage("tableformat.left_begin") + Bukkit
                                                .getOfflinePlayer(uuid).getName());
                                    }
                                    return;
                                default:
                                    sender.sendMessage(MsgUtil.getMessage("command.wrong-args"));
                            }
                        case 2:
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(cmdArg[1]);
                            String offlinePlayerName = offlinePlayer.getName();
                            if (offlinePlayerName == null)
                                offlinePlayerName = "null";
                            switch (cmdArg[0]) {
                                case "add":
                                    shop.addStaff(offlinePlayer.getUniqueId());
                                    sender.sendMessage(MsgUtil.getMessage("shop-staff-added", offlinePlayerName));
                                    return;
                                case "del":
                                    sender.sendMessage(MsgUtil.getMessage("shop-staff-deleted", offlinePlayerName));
                                    return;
                                default:
                                    sender.sendMessage(MsgUtil.getMessage("command.wrong-args"));
                            }
                    }

                }
            }
        }
    }
}