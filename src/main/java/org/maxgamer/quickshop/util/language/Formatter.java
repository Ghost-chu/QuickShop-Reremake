/*
 * This file is a part of project QuickShop, the name is Formatter.java
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

package org.maxgamer.quickshop.util.language;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Formatter {
    /**
     * Calling while a string need to be formatted
     *
     * @param raw    Raw string (but it might formatted by other formatter)
     * @param sender The command sender
     * @param args   The args you can use it to fill
     * @return Formatted string (and it will transfer to other formatters)
     */
    @NotNull String format(@NotNull String raw, @Nullable CommandSender sender, @Nullable String... args);
}
