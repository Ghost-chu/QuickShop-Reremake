package org.maxgamer.quickshop.command.processor;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.util.MsgUtil;

public abstract class ConsoleOnlyCommandProcessor implements ICommandProcessor {

    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (sender instanceof ConsoleCommandSender) {
            onCommand((ConsoleCommandSender) sender, commandLabel, cmdArg);
        } else {
            MsgUtil.sendMessage(sender, "This command is console only!");
        }
    }

    abstract public void onCommand(@NotNull ConsoleCommandSender consoleCommandSender, @NotNull String commandLabel, @NotNull String[] cmdArg);

}
