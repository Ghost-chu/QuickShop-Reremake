package org.maxgamer.quickshop.api.compatibility;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface CompatibilityManager {

    boolean isRegistered(String pluginName);

    /**
     * Switch the compatibility mode on or off, set false to disable all we known incompatiable plugin
     * listener, set true to enable back all disabled plugin liseners. WIP
     *
     * @param status true=turn on closed listeners, false=turn off all turned on listeners.
     * @param player The player to check the listeners
     */
    void toggleProtectionListeners(boolean status, @NotNull Player player);

    void unregisterAll();

    void register(@NotNull CompatibilityModule module);

    void register(@NotNull String moduleName);

    void register(@NotNull Class<? extends CompatibilityModule> compatibilityModuleClass);

    void unregister(@NotNull String moduleName);

    void unregister(@NotNull CompatibilityModule module);
}
