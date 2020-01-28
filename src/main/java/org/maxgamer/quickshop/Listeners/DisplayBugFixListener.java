/*
 * This file is a part of project QuickShop, the name is DisplayBugFixListener.java
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

package org.maxgamer.quickshop.Listeners;

import java.util.Collection;
import lombok.AllArgsConstructor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.DisplayItem;
import org.maxgamer.quickshop.Shop.DisplayType;
import org.maxgamer.quickshop.Util.Util;

@AllArgsConstructor
public class DisplayBugFixListener implements Listener {

  @NotNull private final QuickShop plugin;

  @EventHandler(ignoreCancelled = true)
  public void canBuild(BlockCanBuildEvent e) {
    if (!plugin.isDisplay()
        //|| DisplayItem.getNowUsing(null) != DisplayType.ARMORSTAND
        || e.isBuildable()) {
      return;
    }

    final Collection<Entity> entities =
        e.getBlock().getWorld().getNearbyEntities(e.getBlock().getLocation(), 1.0, 1, 1.0);

    for (Entity entity : entities) {
      if (!(entity instanceof ArmorStand)
          || !DisplayItem.checkIsGuardItemStack(((ArmorStand) entity).getItemInHand())) {
        continue;
      }

      e.setBuildable(true);
      Util.debugLog(
          "Re-set the allowed build flag here because it found the cause of the display-item blocking it before.");
      return;
    }
  }
}
