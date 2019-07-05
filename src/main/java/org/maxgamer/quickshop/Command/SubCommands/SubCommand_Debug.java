package org.maxgamer.quickshop.Command.SubCommands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MsgUtil;

public class SubCommand_Debug implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        boolean debug = plugin.getConfig().getBoolean("dev-mode");
        if (debug) {
            plugin.getConfig().set("dev-mode", false);
            plugin.saveConfig();
            Bukkit.getPluginManager().disablePlugin(plugin);
            Bukkit.getPluginManager().enablePlugin(plugin);
            sender.sendMessage(MsgUtil.getMessage("command.now-nolonger-debuging"));
        } else {
            plugin.getConfig().set("dev-mode", true);
            plugin.saveConfig();
            Bukkit.getPluginManager().disablePlugin(plugin);
            Bukkit.getPluginManager().enablePlugin(plugin);
            sender.sendMessage(MsgUtil.getMessage("command.now-debuging"));
        }
        return ;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }
}
