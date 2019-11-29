package org.maxgamer.quickshop.Listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.Shop.DisplayItem;
import org.maxgamer.quickshop.Util.Util;

import java.util.ArrayList;
import java.util.List;

public class ClearLaggListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void plugin(me.minebuilders.clearlag.events.EntityRemoveEvent clearlaggEvent) {
        if (ListenerHelper.isDisabled(clearlaggEvent.getClass())) {
            return;
        }

        final List<Entity> entities = clearlaggEvent.getEntityList();
        final List<Entity> pendingExclude = new ArrayList<>();

        for (Entity entity : entities) {
            if (!(entity instanceof Item) ||
                !DisplayItem.checkIsGuardItemStack(((Item) entity).getItemStack())) {
                continue;
            }

            pendingExclude.add(entity);
        }

        pendingExclude.forEach(clearlaggEvent::removeEntity);
        Util.debugLog("Prevent " + pendingExclude.size() + " displays removal by ClearLagg.");
    }

}
