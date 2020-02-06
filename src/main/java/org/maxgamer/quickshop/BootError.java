/*
 * This file is a part of project QuickShop, the name is BootError.java
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

package org.maxgamer.quickshop;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/** BootError class contains print errors on /qs command when plugin failed launched. */
@EqualsAndHashCode
@ToString
public class BootError {

  private String[] errors;

  BootError(@NotNull String... errors) {
    this.errors = errors;
    for (String err : errors) {
      QuickShop.instance.getLogger().severe(err);
    }
  }

  /**
   * Print the errors. ##################################################### QuickShop is disabled,
   * Please fix errors and restart ..........................
   * #################################################### This one.
   *
   * @param sender The sender you want output the errors.
   */
  public void printErrors(CommandSender sender) {
    sender.sendMessage(ChatColor.RED + "#####################################################");
    sender.sendMessage(ChatColor.RED + " QuickShop is disabled, Please fix any errors and restart");
    for (String issue : errors) {
      sender.sendMessage(ChatColor.YELLOW + " " + issue);
    }
    // sender.sendMessage(ChatColor.YELLOW+" "+errors);
    sender.sendMessage(ChatColor.RED + "#####################################################");
  }

  public String[] getErrors() {
    return errors;
  }
}
