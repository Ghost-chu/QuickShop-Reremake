package org.maxgamer.quickshop.command.processor;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.util.MsgUtil;

public abstract class PlayerOnlyCommandProcessor implements ICommandProcessor {
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender instanceof Player) {
            onCommand((Player) sender, commandLabel, cmdArg);
        } else {
            MsgUtil.sendMessage(sender, "This command is player only!");
        }
    }

    public abstract void onCommand(@NotNull Player player, @NotNull String commandLabel, @NotNull String[] cmdArg);
}
