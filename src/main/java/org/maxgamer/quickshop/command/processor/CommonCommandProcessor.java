/*
 * This file is a part of project QuickShop, the name is CommonCommandProcessor.java
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

package org.maxgamer.quickshop.command.processor;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

//For backward compatibility, implement CommandProcesser
public abstract class CommonCommandProcessor implements ICommandProcessor {
    /**
     * Accept the onCommand, it will call when have Command Event cmdArg not contains
     * CommandContainer's prefix. E.g: Register the CommandContainer with Prefix: unlimited
     * Permission: quickshop.unlimited
     *
     * <p>When player type /qs unlimited 123 the content of cmdArg is ["123"]
     *
     * @param sender       Sender
     * @param cmdArg       Args
     * @param commandLabel The command prefix /qs is qs
     */
    @Override
    public void onCommand(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {

        throw new UnsupportedOperationException("Unimplemented command");
    }

    /**
     * Accept the onTabComplete, it will call when have Tab Event cmdArg not contains
     * CommandContainer's prefix. E.g: Register the CommandContainer with Prefix: unlimited
     * Permission: quickshop.unlimited
     *
     * <p>When player type /qs unlimited 123 cmdArg's content is 123
     *
     * @param sender       Sender
     * @param cmdArg       Args
     * @param commandLabel The command prefix /qs is qs
     * @return The result for tab-complete lists
     */
    @Override
    @Nullable
    public List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg) {
        return Collections.emptyList();
    }
}
