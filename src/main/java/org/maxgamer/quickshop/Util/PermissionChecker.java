package org.maxgamer.quickshop.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.maxgamer.quickshop.QuickShop;

public class PermissionChecker {
    private QuickShop plugin;
    private boolean usePermissionChecker;

    public PermissionChecker(QuickShop plugin) {
        this.plugin = plugin;
        usePermissionChecker = this.plugin.getConfig().getBoolean("shop.protection-checking");
    }

    public boolean canBuild(Player player, Location location, boolean place) {
        if (!usePermissionChecker) {
            return true;
        } else {
            return canBuild(player, location.getBlock(), place);
        }
    }

    public boolean canBuild(Player player, Block block, boolean place) {
        if (!usePermissionChecker)
            return true;
        BlockEvent beMainHand;
        if (place) {
            beMainHand = new BlockPlaceEvent(block, block.getState(), block.getRelative(0, -1, 0), player.getInventory()
                    .getItemInMainHand(), player, true, EquipmentSlot.HAND);
        } else {
            beMainHand = new BlockBreakEvent(block, player);
        }
        Bukkit.getPluginManager().callEvent(beMainHand);

        if (((Cancellable) beMainHand).isCancelled())
            return false;

        if (beMainHand instanceof BlockPlaceEvent)
            if (!((BlockPlaceEvent) beMainHand).canBuild())
                return false;

        return true;
    }

}
