/*
 * This file is a part of project QuickShop, the name is Compatibility.java
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

package org.maxgamer.quickshop.Util;

import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;

/**
 * WIP
 */
public class Compatibility {
    private final ArrayList<RegisteredListener> disabledListeners = new ArrayList<>();
    // private QuickShop plugin;

    public Compatibility(@NotNull QuickShop plugin) {
        // this.plugin = plugin;
    }

    /**
     * Switch the compatibility mode on or off, set false to disable all we known incompatiable plugin
     * listener, set true to enable back all disabled plugin liseners. WIP
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

    private void enableListeners(@NotNull Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
            Util.debugLog(
                "Calling NoCheatPlus continue follow " + player.getName() + " cheats detection.");
            NCPExemptionManager.unexempt(player);
        }
    }

    private void disableListeners(@NotNull Player player) {
        if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
            Util.debugLog(
                "Calling NoCheatPlus ignore "
                    + player.getName()
                    + " cheats detection until we finished permission checks.");
            NCPExemptionManager.exemptPermanently(player);
        }
    }

}
