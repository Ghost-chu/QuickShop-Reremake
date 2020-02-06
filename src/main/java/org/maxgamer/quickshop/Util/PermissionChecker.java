/*
 * This file is a part of project QuickShop, the name is PermissionChecker.java
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

package org.maxgamer.quickshop.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Event.ProtectionCheckStatus;
import org.maxgamer.quickshop.Event.ShopProtectionCheckEvent;
import org.maxgamer.quickshop.QuickShop;

public class PermissionChecker {
  private QuickShop plugin;
  private boolean usePermissionChecker;

  public PermissionChecker(@NotNull QuickShop plugin) {
    this.plugin = plugin;
    usePermissionChecker = this.plugin.getConfig().getBoolean("shop.protection-checking");
  }

  /**
   * Check player can build in target location
   *
   * @param player Target player
   * @param location Target location
   * @return Success
   */
  public boolean canBuild(@NotNull Player player, @NotNull Location location) {
    return canBuild(player, location.getBlock());
  }

  /**
   * Check player can build in target block
   *
   * @param player Target player
   * @param block Target block
   * @return Success
   */
  public boolean canBuild(@NotNull Player player, @NotNull Block block) {
    if (!usePermissionChecker) {
      return true;
    }
    BlockBreakEvent beMainHand;
    // beMainHand = new BlockPlaceEvent(block, block.getState(), block.getRelative(0, -1, 0),
    // player.getInventory()
    // getItemInMainHand(), player, true, EquipmentSlot.HAND);

    beMainHand = new BlockBreakEvent(block, player);
    // Call for event for protection check start
    Bukkit.getPluginManager()
        .callEvent(
            new ShopProtectionCheckEvent(
                block.getLocation(), player, ProtectionCheckStatus.BEGIN, beMainHand));
    beMainHand.setDropItems(false);
    beMainHand.setExpToDrop(-1);
    Bukkit.getPluginManager().callEvent(beMainHand);
    // Call for event for protection check end
    Bukkit.getPluginManager()
        .callEvent(
            new ShopProtectionCheckEvent(
                block.getLocation(), player, ProtectionCheckStatus.END, beMainHand));
    return !beMainHand.isCancelled();
  }
}
