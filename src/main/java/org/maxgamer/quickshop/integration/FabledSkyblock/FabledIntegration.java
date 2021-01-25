package org.maxgamer.quickshop.integration.fabledskyblock;

import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.island.Island;
import com.songoda.skyblock.island.IslandRole;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.integration.IntegrateStage;
import org.maxgamer.quickshop.integration.IntegrationStage;
import org.maxgamer.quickshop.integration.QSIntegratedPlugin;

@IntegrationStage(loadStage = IntegrateStage.onEnableAfter)
public class FabledIntegration extends QSIntegratedPlugin {

    private final boolean ignoreDisabledWorlds;
    private final boolean whitelist;

    public FabledIntegration(QuickShop plugin) {
        super(plugin);
        ignoreDisabledWorlds = plugin.getConfig().getBoolean("integration.fabledskyblock.ignore-disabled-worlds");
        whitelist = plugin.getConfig().getBoolean("integration.fabledskyblock.whitelist-mode");
    }

    /**
     * Return the integrated plugin name.
     * For example, Residence
     *
     * @return integrated plugin
     */
    @Override
    public @NotNull String getName() {
        return "FabledSkyblock";
    }

    /**
     * Check if a player can create shop here
     *
     * @param player   the player want to create shop
     * @param location shop location
     * @return If you can create shop here
     */
    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
        Island island = SkyBlock.getInstance().getIslandManager().getIslandAtLocation(location);
        if (island == null) return whitelist;
        return island.getRole(player).equals(IslandRole.Member) || island.getRole(player).equals(IslandRole.Owner)
                || island.getRole(player).equals(IslandRole.Operator);
    }

    /**
     * Check if a player can trade with shop here
     *
     * @param player   the player want to trade with shop
     * @param location shop location
     * @return If you can trade with shop here
     */
    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        if (SkyBlock.getInstance().getIslandManager().getIslandAtLocation(location) == null) {
            return ignoreDisabledWorlds;
        }
        return true;
    }

    /**
     * Check if a player can delete a shop here
     *
     * @param player   the player want to delete the shop
     * @param location shop location
     * @return If you can delete the shop here
     */
    @Override
    public boolean canDeleteShopHere(@NotNull Player player, @NotNull Location location) {
        Island island = SkyBlock.getInstance().getIslandManager().getIslandAtLocation(location);
        if (island == null) return whitelist;
        return island.getRole(player).equals(IslandRole.Member) || island.getRole(player).equals(IslandRole.Owner)
                || island.getRole(player).equals(IslandRole.Operator);
    }

    /**
     * Loading logic
     * Execute Stage defined by IntegrationStage
     */
    @Override
    public void load() {

    }

    /**
     * Unloding logic
     * Will execute when Quickshop unloading
     */
    @Override
    public void unload() {

    }
}
