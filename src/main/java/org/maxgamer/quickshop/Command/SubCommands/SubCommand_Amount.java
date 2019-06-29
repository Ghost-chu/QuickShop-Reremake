package org.maxgamer.quickshop.Command.SubCommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MsgUtil;

public class SubCommand_Amount implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            sender.sendMessage("Missing amount");
            return true;
        }

        if (sender instanceof Player) {
            final Player player = (Player) sender;
            if (!plugin.getShopManager().getActions().containsKey(player.getUniqueId())) {
                sender.sendMessage("You do not have any pending action!");
                return true;
            }
            plugin.getShopManager().handleChat(player, cmdArg[0]);
        } else {
            sender.sendMessage("This command can't be run by console");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        ArrayList<String> list = new ArrayList<>();
        list.add(MsgUtil.getMessage("tabcomplete.amount"));
        return list;
    }
}
