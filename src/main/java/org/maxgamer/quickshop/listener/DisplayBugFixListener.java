/*
 * This file is a part of project QuickShop, the name is DisplayBugFixListener.java
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

package org.maxgamer.quickshop.listener;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.shop.DisplayItem;
import org.maxgamer.quickshop.shop.DisplayType;
import org.maxgamer.quickshop.util.Util;

import java.util.Collection;


public class DisplayBugFixListener extends QSListener {
    public DisplayBugFixListener(QuickShop plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void canBuild(BlockCanBuildEvent e) {
        if (!plugin.isDisplay()
                || DisplayItem.getNowUsing() != DisplayType.ARMORSTAND
                || e.isBuildable()) {
            return;
        }

        final Collection<Entity> entities =
                e.getBlock().getWorld().getNearbyEntities(e.getBlock().getLocation(), 1.0, 1, 1.0);

        for (final Entity entity : entities) {
            if (!(entity instanceof ArmorStand)
                    || !DisplayItem.checkIsGuardItemStack(((ArmorStand) entity).getItemInHand())) { //FIXME: Update this when drop 1.13 supports
                continue;
            }

            e.setBuildable(true);
            Util.debugLog(
                    "Re-set the allowed build flag here because it found the cause of the display-item blocking it before.");
            return;
        }
    }

}
