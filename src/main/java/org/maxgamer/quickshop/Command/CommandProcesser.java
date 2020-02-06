/*
 * This file is a part of project QuickShop, the name is CommandProcesser.java
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

package org.maxgamer.quickshop.Command;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CommandProcesser {
  /**
   * Accept the onCommand, it will call when have Command Event cmdArg not contains
   * CommandContainer's prefix. E.g: Register the CommandContainer with Prefix: unlimited
   * Permission: quickshop.unlimited
   *
   * <p>When player type /qs unlimited 123 cmdArg's content is 123
   *
   * @param sender Sender
   * @param cmdArg Args
   * @param commandLabel The command prefix /qs is qs
   */
  void onCommand(
      @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg);

  /**
   * Accept the onTabComplete, it will call when have Tab Event cmdArg not contains
   * CommandContainer's prefix. E.g: Register the CommandContainer with Prefix: unlimited
   * Permission: quickshop.unlimited
   *
   * <p>When player type /qs unlimited 123 cmdArg's content is 123
   *
   * @param sender Sender
   * @param cmdArg Args
   * @param commandLabel The command prefix /qs is qs
   * @return The result for tab-complete lists
   */
  @Nullable
  List<String> onTabComplete(
      @NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] cmdArg);
}
