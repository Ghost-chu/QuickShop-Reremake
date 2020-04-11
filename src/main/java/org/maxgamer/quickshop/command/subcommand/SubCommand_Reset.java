/*
 * This file is a part of project QuickShop, the name is SubCommand_Find.java
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

package org.maxgamer.quickshop.command.subcommand;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.command.CommandProcesser;
import org.maxgamer.quickshop.util.MsgUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SubCommand_Reset implements CommandProcesser {

    private final QuickShop plugin = QuickShop.instance;

    @Override
    @SneakyThrows
    public void onCommand(
        @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {

        if (cmdArg.length < 1) {
            MsgUtil.sendMessage(sender,MsgUtil.getMessage("command.no-type-given", sender));
            return;
        }

        switch (cmdArg[0]) {
            case "lang":
                File cache = new File(plugin.getDataFolder(), "cache");
                File item = new File(plugin.getDataFolder(), "itemi18n.yml");
                File ench = new File(plugin.getDataFolder(), "enchi18n.yml");
                File potion = new File(plugin.getDataFolder(), "potioni18n.yml");
                cache.delete();
                item.delete();
                ench.delete();
                potion.delete();
                MsgUtil.loadGameLanguage(Objects.requireNonNull(plugin.getConfig().getString("game-language", "default")));
                MsgUtil.loadItemi18n();
                MsgUtil.loadEnchi18n();
                MsgUtil.loadPotioni18n();
                MsgUtil.sendMessage(sender,MsgUtil.getMessage("complete", sender));
                break;
            case "config":
                File config = new File(plugin.getDataFolder(), "config.yml");
                config.delete();
                plugin.saveDefaultConfig();
                plugin.reloadConfig();
                Bukkit.getPluginManager().disablePlugin(plugin);
                Bukkit.getPluginManager().enablePlugin(plugin);
                MsgUtil.sendMessage(sender,MsgUtil.getMessage("complete", sender));
                break;
            case "messages":
                File msgs = new File(plugin.getDataFolder(), "messages.json");
                msgs.delete();
                MsgUtil.loadCfgMessages();
                MsgUtil.sendMessage(sender,MsgUtil.getMessage("complete", sender));
                break;
        }
    }

    @NotNull
    @Override
    public List<String> onTabComplete(
        @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        ArrayList<String> tab = new ArrayList<>();
        tab.add("lang");
        tab.add("config");
        tab.add("messages");
        return tab;
    }

}
