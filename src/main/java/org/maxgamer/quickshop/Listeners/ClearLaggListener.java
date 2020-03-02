/*
 * This file is a part of project QuickShop, the name is ClearLaggListener.java
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

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.Shop.DisplayItem;
import org.maxgamer.quickshop.Util.Util;

public class ClearLaggListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void plugin(me.minebuilders.clearlag.events.EntityRemoveEvent clearlaggEvent) {
        final List<Entity> entities = clearlaggEvent.getEntityList();
        final List<Entity> pendingExclude = new ArrayList<>();

        for (Entity entity : entities) {
            if (!(entity instanceof Item)
                || !DisplayItem.checkIsGuardItemStack(((Item) entity).getItemStack())) {
                continue;
            }

            pendingExclude.add(entity);
        }

        pendingExclude.forEach(clearlaggEvent::removeEntity);
        Util.debugLog("Prevent " + pendingExclude.size() + " displays removal by ClearLagg.");
    }

}
