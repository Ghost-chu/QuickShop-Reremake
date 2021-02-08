/*
 * This file is a part of project QuickShop, the name is SubCommand_Paste.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.command.subcommand;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.util.MsgUtil;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.paste.Paste;

@AllArgsConstructor
public class SubCommand_Paste implements CommandProcesser {

  private final QuickShop plugin;

  @Override
  public void onCommand(@NotNull CommandSender sender,
                        @NotNull String commandLabel,
                        @NotNull String[] cmdArg) {
    // do actions
    new BukkitRunnable() {
      @Override
      public void run() {
        if (Bukkit.getPluginManager().getPlugin("ConsoleSpamFix") != null) {
          if (cmdArg.length < 1) {
            sender.sendMessage(
                "Warning: ConsoleSpamFix installed! Please disable it before reporting any errors!");
            return;
          } else {
            if (Arrays.stream(cmdArg).noneMatch(
                    str -> str.contains("--force"))) {
              sender.sendMessage(
                  "Warning: ConsoleSpamFix installed! Please disable it before reporting any errors!");
              return;
            }
          }
        }

        if (Arrays.stream(cmdArg).anyMatch(str -> str.contains("file"))) {
          pasteToLocalFile(sender);
          return;
        }
        MsgUtil.sendMessage(
            sender,
            "§aPlease wait, we're uploading the data to the pastebin...");
        if (!pasteToPastebin(sender)) {
          MsgUtil.sendMessage(
              sender,
              "The paste failed, saving the paste at local location...");
          pasteToLocalFile(sender);
        }
      }
    }.runTaskAsynchronously(plugin);
  }

  private boolean pasteToPastebin(@NotNull CommandSender sender) {
    final Paste paste = new Paste(plugin);
    final String pasteText = paste.genNewPaste();
    String pasteResult = paste.paste(pasteText);
    if (pasteResult != null) {
      sender.sendMessage(pasteResult);
      plugin.log(pasteResult);
      return true;
    }
    return false;
  }

  private boolean pasteToLocalFile(@NotNull CommandSender sender) {
    File file = new File(plugin.getDataFolder(), "paste");
    file.mkdirs();
    file = new File(file, "paste-" +
                              UUID.randomUUID().toString().replaceAll("-", "") +
                              ".txt");
    final Paste paste = new Paste(plugin);
    final String pasteText = paste.genNewPaste();
    try {
      boolean createResult = file.createNewFile();
      Util.debugLog("Create paste file: " + file.getCanonicalPath() + " " +
                    createResult);
      try (FileWriter fwriter = new FileWriter(file)) {
        fwriter.write(pasteText);
        fwriter.flush();
      }
      MsgUtil.sendMessage(sender, "Paste was saved to your server at: " +
                                      file.getAbsolutePath());
      return true;
    } catch (IOException e) {
      plugin.getSentryErrorReporter().ignoreThrow();
      plugin.getLogger().log(
          Level.WARNING,
          "Failed to save paste to your local drive, the content will print to Console",
          e);
      MsgUtil.sendMessage(sender, "Saving failed, output to console...");
      plugin.getLogger().info(pasteText);
      return false;
    }
  }
}
