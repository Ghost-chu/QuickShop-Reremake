package org.maxgamer.quickshop.util.compatibility;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class CompatibilityManager {
    private final Set<CompatibilityModule> registeredModules = new HashSet<>(5);

    /**
     * Switch the compatibility mode on or off, set false to disable all we known incompatiable plugin
     * listener, set true to enable back all disabled plugin liseners. WIP
     *
     * @param status true=turn on closed listeners, false=turn off all turned on listeners.
     * @param player The player to check the listeners
     */
    public void toggleProtectionListeners(boolean status, @NotNull Player player) {
        this.registeredModules.forEach(module -> module.toggle(player, status));
    }

    public void register(@NotNull CompatibilityModule module) {
        registeredModules.add(module);
    }

    public void unregister(@NotNull CompatibilityModule module) {
        registeredModules.remove(module);
    }
}
