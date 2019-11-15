package org.maxgamer.quickshop.Util;

import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;

import java.util.ArrayList;

/**
 * WIP
 */
public class Compatibility {
    private final ArrayList<RegisteredListener> disabledListeners = new ArrayList<>();
    private QuickShop plugin;

    public Compatibility(@NotNull QuickShop plugin) {
        this.plugin = plugin;
    }

    private void disableListeners(@NotNull Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
            Util.debugLog("Calling NoCheatPlus ignore " + player
                    .getName() + " cheats detection until we finished permission checks.");
            NCPExemptionManager.exemptPermanently(player);
        }
    }

    private void enableListeners(@NotNull Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
            Util.debugLog("Calling NoCheatPlus continue follow " + player.getName() + " cheats detection.");
            NCPExemptionManager.unexempt(player);
        }
    }

    /**
     * Switch the compatibility mode on or off, set false to disable all we known incompatiable plugin listener,
     * set true to enable back all disabled plugin liseners.
     * WIP
     *
     * @param status true=turn on closed listeners, false=turn off all turned on listeners.
     * @param player The player to check the listeners
     */
    public void toggleProtectionListeners(boolean status, @NotNull Player player) {
        if (status) {
            enableListeners(player);
        } else {
            disableListeners(player);
        }
    }
}
