package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Paste;
import org.maxgamer.quickshop.Util.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                sender.sendMessage("Please wait...");
                Paste paste = new Paste(plugin);
                String pasteText = paste.genNewPaste();
                try {
                    sender.sendMessage(paste.pasteTheText(pasteText));
                } catch (Exception err) {
                    sender.sendMessage("The paste failed, saving the paste at local location...");
                    File file = new File(plugin.getDataFolder(),"paste");
                    file.mkdirs();
                    file = new File(file, "paste-"+UUID.randomUUID().toString().replaceAll("-","")+".txt");
                    try {
                        boolean createResult = file.createNewFile();
                        Util.debugLog("Create paste file: "+file.getCanonicalPath()+" "+createResult);
                        FileWriter fwriter = new FileWriter(file);
                        fwriter.write(pasteText);
                        fwriter.flush();
                        fwriter.close();
                        sender.sendMessage("Paste was saved to your server at: " + file.getAbsolutePath());
                    } catch (IOException e) {
                        plugin.getSentryErrorReporter().ignoreThrow();
                        e.printStackTrace();
                        sender.sendMessage("Saving failed, output to console...");
                        plugin.getLogger().info(pasteText);
                        return;
                    }

                }
            }
        }.runTaskAsynchronously(plugin);
    }
}
