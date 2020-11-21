package org.maxgamer.quickshop.integration;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface IntegratedPlugin {
    /**
     * Return the integrated plugin name.
     * For example, Residence
     *
     * @return integrated plugin
     */
    @NotNull String getName();

    /**
     * Check if a player can create shop here
     *
     * @param player   the player want to create shop
     * @param location shop location
     * @return If you can create shop here
     */
    boolean canCreateShopHere(@NotNull Player player, @NotNull Location location);

    /**
     * Check if a player can trade with shop here
     *
     * @param player   the player want to trade with shop
     * @param location shop location
     * @return If you can trade with shop here
     */
    boolean canTradeShopHere(@NotNull Player player, @NotNull Location location);

    /**
     * Check if a player can delete a shop here
     *
     * @param player   the player want to delete the shop
     * @param location shop location
     * @return If you can delete the shop here
     */
    default boolean canDeleteShopHere(@NotNull Player player, @NotNull Location location) {
        return false;
    }

    /**
     * Loading logic
     * Execute Stage defined by IntegrationStage
     */
    void load();

    /**
     * Unloding logic
     * Will execute when Quickshop unloading
     */
    void unload();
}
