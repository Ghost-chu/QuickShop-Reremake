package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MsgUtil;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_Reload implements CommandProcesser {

    private final QuickShop plugin = QuickShop.instance;

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        sender.sendMessage(MsgUtil.getMessage("command.reloading", sender));
        plugin.getServer().getPluginManager().disablePlugin(plugin);
        plugin.getServer().getPluginManager().enablePlugin(plugin);
    }
}
