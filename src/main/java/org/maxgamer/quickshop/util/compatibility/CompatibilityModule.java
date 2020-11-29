package org.maxgamer.quickshop.util.compatibility;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface CompatibilityModule {
    /**
     * Gets the CompatibilityModule provider name
     *
     * @return Provider name
     */
    @NotNull String getName();

    /**
     * Gets the CompatibilityModule provider plugin instance
     *
     * @return Provider Plugin instance
     */
    @NotNull Plugin getPlugin();

    /**
     * Calls CompatibilityModule to toggle the detection status for player between on and off
     *
     * @param player   The player
     * @param checking On or Off
     */
    void toggle(@NotNull Player player, boolean checking);
}
