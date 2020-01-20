/*
 * This file is a part of project QuickShop, the name is SubCommand_ROOT.java
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
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Command.CommandContainer;
import org.maxgamer.quickshop.Command.CommandProcesser;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class SubCommand_ROOT implements CommandProcesser {

    private final QuickShop plugin = QuickShop.instance;

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] strings) {
        final List<String> candidate = new ArrayList<>();

        for (CommandContainer container : plugin.getCommandManager().getCmds()) {
            if (!container.getPrefix().startsWith(strings[0]) && !container.getPrefix().equals(strings[0])) {
                continue;
            }

            final List<String> requirePermissions = container.getPermissions();

            if (requirePermissions != null) {
                for (String requirePermission : requirePermissions) {
                    if (requirePermission != null && !requirePermission.isEmpty() && !QuickShop.getPermissionManager().hasPermission(sender, requirePermission)) {
                        Util.debugLog("Sender " + sender
                                .getName() + " trying tab-complete the command: " + commandLabel + ", but no permission " + requirePermission);
                        return new ArrayList<>();
                    }
                }
            }

            if (!container.isHidden()) {
                candidate.add(container.getPrefix());
            }
        }

        return candidate;
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        new SubCommand_Help().onCommand(sender, commandLabel, cmdArg);
    }
}

