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
        Bukkit.getPluginManager().callEvent(new ShopProtectionCheckEvent(block.getLocation(), player, ProtectionCheckStatus.BEGIN, beMainHand));
        ListenerHelper.disableEvent(beMainHand.getClass());
        //Bukkit.getPluginManager().callEvent(beMainHand);
        beMainHand.setDropItems(false);
        beMainHand.setExpToDrop(-1);
        Plugin cancelPlugin = null;
        if (plugin.getConfig().getBoolean("shop.use-protection-checking-filter")) {
            cancelPlugin = plugin.getQsEventManager().fireEvent(beMainHand);
        } else {
            Bukkit.getPluginManager().callEvent(beMainHand);
        }
        //Use our custom event caller.
        ListenerHelper.enableEvent(beMainHand.getClass());
        //Call for event for protection check end
        Bukkit.getPluginManager().callEvent(new ShopProtectionCheckEvent(block.getLocation(), player, ProtectionCheckStatus.END, beMainHand));
        boolean canBuild = !((Cancellable) beMainHand).isCancelled();

        if (!canBuild) {
//            Util.debugLog("Somethings say build check failed, there is HandlerList to help you debug: ");
//            for (RegisteredListener listener : beMainHand.getHandlers().getRegisteredListeners()) {
//                Util.debugLog("- " + listener.getPlugin().getName() + " : " + listener.getListener().getClass().getSimpleName());
//            }
            if (cancelPlugin != null) {
                Util.debugLog("Plugin " + cancelPlugin.getName() + " cancelled this build create action.");
            }
        }

        return canBuild;
        // if (beMainHand instanceof BlockPlaceEvent)
        //     return ((BlockPlaceEvent) beMainHand).canBuild();

    }

}
