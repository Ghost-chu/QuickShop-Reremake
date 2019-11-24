package org.maxgamer.quickshop.Listeners;

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

import java.util.Collection;

@AllArgsConstructor
public class DisplayBugFixListener implements Listener {

    @NotNull
    private final QuickShop plugin;

    @EventHandler(ignoreCancelled = true)
    public void canBuild(BlockCanBuildEvent e) {
        if (ListenerHelper.isDisabled(e.getClass()) ||
            !plugin.isDisplay() ||
            DisplayItem.getNowUsing() != DisplayType.ARMORSTAND ||
            e.isBuildable()) {
            return;
        }

        final Collection<Entity> entities = e.getBlock().getWorld().getNearbyEntities(e.getBlock().getLocation(), 1.0, 1, 1.0);

        for (Entity entity : entities) {
            if (!(entity instanceof ArmorStand) ||
                !DisplayItem.checkIsGuardItemStack(((ArmorStand) entity).getItemInHand())) {
                continue;
            }

            e.setBuildable(true);
            Util.debugLog("Re-set the allowed build flag here because it found the cause of the display-item blocking it before.");
            return;
        }
    }
}
