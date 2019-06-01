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
        BlockEvent beOffHand;
        if (place) {
            beMainHand = new BlockPlaceEvent(block, block.getState(), block.getRelative(0, -1, 0), player.getInventory()
                    .getItemInMainHand(), player, true, EquipmentSlot.HAND);
            beOffHand = new BlockPlaceEvent(block, block.getState(), block.getRelative(0, -1, 0), player.getInventory()
                    .getItemInOffHand(), player, true, EquipmentSlot.OFF_HAND);
        } else {
            beMainHand = new BlockBreakEvent(block, player);
            beOffHand = new BlockBreakEvent(block, player);
        }
        Bukkit.getPluginManager().callEvent(beMainHand);
        Bukkit.getPluginManager().callEvent(beOffHand);

        if (((Cancellable) beMainHand).isCancelled())
            return false;
        if (((Cancellable) beOffHand).isCancelled())
            return false;

        if (beMainHand instanceof BlockPlaceEvent)
            if (!((BlockPlaceEvent) beMainHand).canBuild())
                return false;

        if (beOffHand instanceof BlockPlaceEvent)
            if (!((BlockPlaceEvent) beOffHand).canBuild())
                return false;

        return true;
    }

}
