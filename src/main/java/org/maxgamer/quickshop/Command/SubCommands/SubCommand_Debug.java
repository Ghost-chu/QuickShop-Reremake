package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MsgUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SubCommand_Debug implements CommandProcesser {

    private final QuickShop plugin = QuickShop.instance;

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        final ArrayList<String> list = new ArrayList<>();

        list.add("debug");
        list.add("dev");
        list.add("devmode");
        list.add("handlerlist");

        return list;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            switchDebug(sender);
            return;
        }

        switch (cmdArg[0]) {
            case "debug":
            case "dev":
            case "devmode":
                switchDebug(sender);
                break;
            case "handlerlist":
                if (cmdArg.length < 2) {
                    sender.sendMessage("You must given a event");
                    break;
                }

                printHandlerList(sender, cmdArg[1]);
                break;
            default:
                sender.sendMessage("Error, no correct args given.");
                break;
        }
    }

    public void switchDebug(@NotNull CommandSender sender) {
        final boolean debug = plugin.getConfig().getBoolean("dev-mode");

        if (debug) {
            plugin.getConfig().set("dev-mode", false);
            plugin.saveConfig();
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            plugin.getServer().getPluginManager().enablePlugin(plugin);
            sender.sendMessage(MsgUtil.getMessage("command.now-nolonger-debuging", sender));
            return;
        }

        plugin.getConfig().set("dev-mode", true);
        plugin.saveConfig();
        plugin.getServer().getPluginManager().disablePlugin(plugin);
        plugin.getServer().getPluginManager().enablePlugin(plugin);
        sender.sendMessage(MsgUtil.getMessage("command.now-debuging", sender));
    }

    public void printHandlerList(@NotNull CommandSender sender, String event) {
        try {
            final Class clazz = Class.forName(event);
            final Method method = clazz.getMethod("getHandlerList");
            final Object[] obj = new Object[0];
            final HandlerList list = (HandlerList) method.invoke(null, obj);

            for (RegisteredListener listener1 : list.getRegisteredListeners()) {
                sender.sendMessage(ChatColor.AQUA + listener1.getPlugin().getName() + ChatColor.YELLOW + " # " + ChatColor.GREEN + listener1.getListener().getClass().getCanonicalName());
            }
        } catch (Throwable th) {
            sender.sendMessage("ERR " + th.getMessage());
            th.printStackTrace();
        }
    }
}
