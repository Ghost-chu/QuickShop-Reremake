package org.maxgamer.quickshop.Util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.*;
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
     * @param place    Place block (false = break block)
     * @return Success
     */
    public boolean canBuild(@NotNull Player player, @NotNull Location location, boolean place) {
        return canBuild(player, location.getBlock(), place);
    }

    /**
     * Check player can build in target block
     * @param player Target player
     * @param block Target block
     * @param place Place block (false = break block)
     * @return Success
     */
    public boolean canBuild(@NotNull Player player, @NotNull Block block, boolean place) {
        if (!usePermissionChecker)
            return true;
        BlockEvent beMainHand;
        // beMainHand = new BlockPlaceEvent(block, block.getState(), block.getRelative(0, -1, 0), player.getInventory()
        //getItemInMainHand(), player, true, EquipmentSlot.HAND);

        beMainHand = new BlockBreakEvent(block, player);
        ListenerHelper.disableEvent(beMainHand.getClass());
        Bukkit.getPluginManager().callEvent(beMainHand);
        ListenerHelper.enableEvent(beMainHand.getClass());
        Util.debugLog("HandlerList: ");
        for (RegisteredListener listener : beMainHand.getHandlers().getRegisteredListeners()) {
            Util.debugLog("- " + listener.getPlugin().getName() + " : " + listener.getListener().getClass().getSimpleName());
        }

        return !((Cancellable) beMainHand).isCancelled();
        // if (beMainHand instanceof BlockPlaceEvent)
        //     return ((BlockPlaceEvent) beMainHand).canBuild();

    }

}
