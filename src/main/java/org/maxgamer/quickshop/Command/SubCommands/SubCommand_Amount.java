package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MsgUtil;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_Amount implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        ArrayList<String> list = new ArrayList<>();
        list.add(MsgUtil.getMessage("tabcomplete.amount", sender));
        return list;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            sender.sendMessage(MsgUtil.getMessage("command.wrong-args", sender));
            return;
        }

        if (sender instanceof Player) {
            final Player player = (Player) sender;
            if (!plugin.getShopManager().getActions().containsKey(player.getUniqueId())) {
                sender.sendMessage(MsgUtil.getMessage("no-pending-action", sender));
                return;
            }
            plugin.getShopManager().handleChat(player, cmdArg[0]);
        } else {
            sender.sendMessage("This command can't be run by console");
        }
    }
}
