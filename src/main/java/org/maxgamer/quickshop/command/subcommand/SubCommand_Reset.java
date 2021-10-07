/*
 * This file is a part of project QuickShop, the name is SubCommand_Reset.java
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

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.api.command.CommandHandler;
import org.maxgamer.quickshop.util.MsgUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class SubCommand_Reset implements CommandHandler<CommandSender> {

    private final QuickShop plugin;
    private final List<String> tabCompleteList = Collections.unmodifiableList(
            Arrays.asList("lang", "config", "messages")
    );


    @Override
    @SneakyThrows
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        if (cmdArg.length < 1) {
            plugin.text().of(sender, "command.no-type-given").send();
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
                plugin.text().of(sender, "complete").send();
                break;
            case "config":
                File config = new File(plugin.getDataFolder(), "config.yml");
                config.delete();
                plugin.saveDefaultConfig();
                plugin.reloadConfig();
                plugin.getServer().getPluginManager().disablePlugin(plugin);
                plugin.getServer().getPluginManager().enablePlugin(plugin);
                plugin.text().of(sender, "complete").send();
                break;
            case "messages":
                File msgs = new File(plugin.getDataFolder(), "messages.json");
                msgs.delete();
                MsgUtil.loadI18nFile();
                plugin.text().of(sender, "complete").send();
                break;
            default:
                plugin.text().of(sender, "command.wrong-args").send();
        }
    }

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return tabCompleteList;
    }

}
