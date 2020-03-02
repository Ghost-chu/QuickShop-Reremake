/*
 * This file is a part of project QuickShop, the name is SubCommand_Amount.java
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;

public class SubCommand_Export implements CommandProcesser {

    private final QuickShop plugin = QuickShop.instance;

    @Override
    @SneakyThrows
    public synchronized void onCommand(
        @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (!(sender instanceof ConsoleCommandSender)) {
            return;
        }
        File file = new File(plugin.getDataFolder(), "export.txt");
        if (file.exists()) {
            file.delete();
            file.createNewFile();
        } else {
            file.createNewFile();
        }

        new BukkitRunnable() {
            @SneakyThrows
            @Override
            public void run() {
                StringBuilder finalReport = new StringBuilder();
                plugin
                    .getShopLoader()
                    .getShopsInDatabase()
                    .forEach((shop -> finalReport.append("\t").append(shop).append("\n")));
                BufferedWriter outputStream = new BufferedWriter(new FileWriter(file, false));
                outputStream.write(finalReport.toString());
                outputStream.close();
                sender.sendMessage("Done.");
            }
        }.runTaskAsynchronously(plugin);


    }

    @NotNull
    @Override
    public List<String> onTabComplete(
        @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return null;
    }

}
