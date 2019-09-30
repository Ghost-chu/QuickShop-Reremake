package org.maxgamer.quickshop.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.Event.ProtectionCheckStatus;
import org.maxgamer.quickshop.Event.ShopProtectionCheckEvent;
import org.maxgamer.quickshop.Listeners.ListenerHelper;
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
     * @param player   Target player
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
     * @param block  Target block
     * @return Success
     */
    public boolean canBuild(@NotNull Player player, @NotNull Block block) {
        if (!usePermissionChecker) {
            return true;
        }
        BlockBreakEvent beMainHand;
        // beMainHand = new BlockPlaceEvent(block, block.getState(), block.getRelative(0, -1, 0), player.getInventory()
        //getItemInMainHand(), player, true, EquipmentSlot.HAND);

        beMainHand = new BlockBreakEvent(block, player);
        //Call for event for protection check start
        ListenerHelper.disableEvent(beMainHand.getClass());
        Bukkit.getPluginManager().callEvent(new ShopProtectionCheckEvent(block.getLocation(), player, ProtectionCheckStatus.BEGIN, beMainHand));
        beMainHand.setDropItems(false);
        beMainHand.setExpToDrop(-1);
        Bukkit.getPluginManager().callEvent(beMainHand);
        //Call for event for protection check end
        Bukkit.getPluginManager().callEvent(new ShopProtectionCheckEvent(block.getLocation(), player, ProtectionCheckStatus.END, beMainHand));
        ListenerHelper.enableEvent(beMainHand.getClass());
        return beMainHand.isCancelled();
    }

}
