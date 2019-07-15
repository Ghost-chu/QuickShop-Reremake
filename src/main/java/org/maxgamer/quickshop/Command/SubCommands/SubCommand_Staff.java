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

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender instanceof Player && sender.hasPermission("quickshop.staff")) {
            BlockIterator bIt = new BlockIterator((LivingEntity) sender, 10);
            if (!bIt.hasNext()) {
                sender.sendMessage(MsgUtil.getMessage("not-looking-at-shop"));
                return;
            }
            while (bIt.hasNext()) {
                Block b = bIt.next();
                Shop shop = plugin.getShopManager().getShop(b.getLocation());
                if (shop != null && shop.getModerator().isModerator(((Player) sender).getUniqueId())) {
                    if (cmdArg.length == 0) { // qs staff
                        // Send
                        sender.sendMessage(MsgUtil.getMessage("command.wrong-args"));
                        return;
                    }
                    if (cmdArg.length == 1) { // qs staff [add|del|clear|others]
                        if (!cmdArg[0].equals("add") && !cmdArg[0].equals("del") && !cmdArg[0].equals("clear")) {
                            sender.sendMessage(MsgUtil.getMessage("command.wrong-args"));
                            return;
                        }
                        if (cmdArg[0].equals("clear")) {
                            shop.clearStaffs();
                            shop.update();
                            sender.sendMessage(MsgUtil.getMessage("shop-staff-cleared"));
                        }
                        if (cmdArg[0].equals("list")) {
                            for (UUID uuid : shop.getStaffs()) {
                                sender.sendMessage(ChatColor.GREEN + MsgUtil.getMessage("tableformat.left_begin") + Bukkit
                                        .getPlayer(uuid).getName());
                            }
                        }
                        sender.sendMessage(MsgUtil.getMessage("command.wrong-args"));
                        return;
                    }
                    if (cmdArg.length == 2) { // qs staff [add|del] [player]
                        if (!cmdArg[0].equals("add") && !cmdArg[0].equals("del")) {
                            sender.sendMessage(MsgUtil.getMessage("command.wrong-args"));
                            return;
                        }
                        @SuppressWarnings("deprecation")
                        OfflinePlayer player = Bukkit.getOfflinePlayer(cmdArg[1]);
                        // if (player == null) {
                        //     sender.sendMessage(MsgUtil.getMessage("unknown-player"));
                        //     return true;
                        // }

                        if (cmdArg[0].equals("add")) {

                            shop.addStaff(player.getUniqueId());
                            sender.sendMessage(MsgUtil.getMessage("shop-staff-added", cmdArg[1]));
                            return;
                        }
                        if (cmdArg[0].equals("del")) {

                            shop.delStaff(player.getUniqueId());
                            sender.sendMessage(MsgUtil.getMessage("shop-staff-deleted", cmdArg[1]));
                            return;
                        }
                    }
                    return;
                }
            }
            return;
        }
        sender.sendMessage(MsgUtil.getMessage("no-permission"));
    }
}
