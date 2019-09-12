package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Paste;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_Paste implements CommandProcesser {
    private QuickShop plugin = QuickShop.instance;

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        //do actions
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    sender.sendMessage("Please wait...");
                    Paste paste = new Paste(plugin);
                    sender.sendMessage(paste.pasteTheText(paste.genNewPaste()));

                } catch (Exception err) {
                    sender.sendMessage("The paste failed, see console for details.");
                }
            }
        }.runTaskAsynchronously(plugin);
    }
}
