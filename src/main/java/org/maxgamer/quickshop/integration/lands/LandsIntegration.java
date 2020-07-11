package org.maxgamer.quickshop.integration.lands;

import me.angeschossen.lands.api.land.Land;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.integration.IntegratedPlugin;

public class LandsIntegration implements IntegratedPlugin {

    private final boolean ignoreDisabledWorlds;
    private final boolean whitelist;
    me.angeschossen.lands.api.integration.LandsIntegration landsIntegration;

    public LandsIntegration(QuickShop plugin) {
        landsIntegration = new me.angeschossen.lands.api.integration.LandsIntegration(plugin);
        ignoreDisabledWorlds = plugin.getConfig().getBoolean("integration.lands.ignore-disabled-worlds");
        whitelist = plugin.getConfig().getBoolean("integration.lands.whitelist-mode");
    }

    @Override
    public @NotNull String getName() {
        return "LandsIntegration";
    }

    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
        if (landsIntegration.getLandWorld(location.getWorld()) == null) {
            return ignoreDisabledWorlds;
        }
        Land land = landsIntegration.getLand(location);
        if (land != null) {
            return land.getOwnerUID().equals(player.getUniqueId()) || land.isTrusted(player.getUniqueId());
        } else {
            return whitelist;
        }
    }

    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        if (landsIntegration.getLandWorld(location.getWorld()) == null) {
            return ignoreDisabledWorlds;
        }
        return true;
    }

    @Override
    public void load() {


    }

    @Override
    public void unload() {

    }
}
