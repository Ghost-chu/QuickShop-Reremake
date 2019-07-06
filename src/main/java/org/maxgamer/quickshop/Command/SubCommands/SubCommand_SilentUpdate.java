package org.maxgamer.quickshop.Command.SubCommands;

import java.io.IOException;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Updater;

public class SubCommand_SilentUpdate implements CommandProcesser {
    QuickShop plugin = QuickShop.instance;

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        sender.sendMessage(ChatColor.YELLOW + "Checking for updates...");
        if (Updater.checkUpdate().getVersion().equals(plugin.getDescription().getVersion())) {
            sender.sendMessage(ChatColor.GREEN + "No updates can update now.");
            return;
        }
        byte[] pluginBin;
        try {
            pluginBin = Updater.downloadUpdatedJar();
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Update failed, get details to look the console.");
            plugin.getSentryErrorReporter().ignoreThrow();
            e.printStackTrace();
            return;
        }
        if (pluginBin == null || pluginBin.length < 1) {
            sender.sendMessage(ChatColor.RED + "Download failed, check your connection before contact the author.");
            return;
        }
        try {
            Updater.replaceTheJar(pluginBin);
        } catch (IOException ioe) {
            sender.sendMessage(ChatColor.RED + "Update failed, get details to look the console.");
            plugin.getSentryErrorReporter().ignoreThrow();
            ioe.printStackTrace();
            return;
        } catch (RuntimeException re) {
            sender.sendMessage(ChatColor.RED + "Update failed, " + re.getMessage());
            return;
        }
        sender.sendMessage(ChatColor.GREEN + "Update successfully, restart your server to apply the changes!");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return null;
    }
}
