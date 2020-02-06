/*
 * This file is a part of project QuickShop, the name is PermissionProvider.java
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

package org.maxgamer.quickshop.Permission;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface PermissionProvider {
  /**
   * Test the sender has special permission
   *
   * @param sender CommandSender
   * @param permission The permission want to check
   * @return hasPermission
   */
  boolean hasPermission(@NotNull CommandSender sender, @NotNull String permission);

  /**
   * Get permission provider name
   *
   * @return The name of permission provider
   */
  @NotNull
  String getName();

  /**
   * Get the debug infos in provider
   *
   * @param sender CommandSender
   * @param permission The permission want to check
   * @return Debug Infos
   */
  @NotNull
  PermissionInfomationContainer getDebugInfo(
      @NotNull CommandSender sender, @NotNull String permission);
}
