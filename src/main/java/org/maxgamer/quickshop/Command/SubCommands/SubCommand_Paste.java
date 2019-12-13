/*
 * This file is a part of project QuickShop, the name is SubCommand_Paste.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.Command.SubCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Paste.Paste;
import org.maxgamer.quickshop.Util.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SubCommand_Paste implements CommandProcesser {

    private final QuickShop plugin = QuickShop.instance;

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return new ArrayList<>();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        //do actions
        new BukkitRunnable() {
            @Override
            public void run() {
                sender.sendMessage("Please wait, we're uploading the data to the pastebin...");
                final Paste paste = new Paste(plugin);
                final String pasteText = paste.genNewPaste();
                    String pasteResult = paste.paste(pasteText);
                    if(pasteResult != null){
                        sender.sendMessage(pasteResult);
                    }else{
                        sender.sendMessage("The paste failed, saving the paste at local location...");
                        File file = new File(plugin.getDataFolder(), "paste");
                        file.mkdirs();
                        file = new File(file, "paste-" + UUID.randomUUID().toString().replaceAll("-", "") + ".txt");
                        try {
                            final boolean createResult = file.createNewFile();
                            Util.debugLog("Create paste file: " + file.getCanonicalPath() + " " + createResult);
                            final FileWriter fwriter = new FileWriter(file);
                            fwriter.write(pasteText);
                            fwriter.flush();
                            fwriter.close();
                            sender.sendMessage("Paste was saved to your server at: " + file.getAbsolutePath());
                        } catch (IOException e) {
                            plugin.getSentryErrorReporter().ignoreThrow();
                            e.printStackTrace();
                            sender.sendMessage("Saving failed, output to console...");
                            plugin.getLogger().info(pasteText);
                        }
                    }


            }
        }.runTaskAsynchronously(plugin);
    }
}
