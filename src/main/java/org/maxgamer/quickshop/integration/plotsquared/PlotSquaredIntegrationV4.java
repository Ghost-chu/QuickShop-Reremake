/*
 * This file is a part of project QuickShop, the name is PlotSquaredIntegrationV4.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.integration.plotsquared;

import com.github.intellectualsites.plotsquared.bukkit.events.PlayerPlotTrustedEvent;
import com.github.intellectualsites.plotsquared.bukkit.events.PlotDeleteEvent;
import com.github.intellectualsites.plotsquared.plot.flag.BooleanFlag;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.integration.IntegrateStage;
import org.maxgamer.quickshop.integration.IntegrationStage;
import org.maxgamer.quickshop.integration.QSIntegratedPlugin;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;
import org.maxgamer.quickshop.util.reload.Reloadable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("DuplicatedCode")
@IntegrationStage(loadStage = IntegrateStage.onEnableAfter)
public class PlotSquaredIntegrationV4 extends QSIntegratedPlugin implements Listener, Reloadable {
    private boolean whiteList;
    private boolean deleteUntrusted;
    private BooleanFlag createFlag;
    private BooleanFlag tradeFlag;

    public PlotSquaredIntegrationV4(QuickShop plugin) {
        super(plugin);
        init();
        plugin.getReloadManager().register(this);
    }

    private void init() {
        this.whiteList = plugin.getConfig().getBoolean("integration.plotsquared.whitelist-mode");
        this.deleteUntrusted = plugin.getConfig().getBoolean("integration.plotsquared.delete-when-user-untrusted");
    }

    @Override
    public @NotNull String getName() {
        return "PlotSquared";
    }

    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
        com.github.intellectualsites.plotsquared.plot.object.Location pLocation =
                new com.github.intellectualsites.plotsquared.plot.object.Location(
                        location.getWorld().getName(),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ());
        Plot plot = pLocation.getPlot();
        if (plot == null) {
            return !whiteList;
        }
        return this.createFlag.isTrue(plot);
    }

    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        com.github.intellectualsites.plotsquared.plot.object.Location pLocation =
                new com.github.intellectualsites.plotsquared.plot.object.Location(
                        location.getWorld().getName(),
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ());
        Plot plot = pLocation.getPlot();
        if (plot == null) {
            return !whiteList;
        }
        return this.tradeFlag.isFalse(plot);
    }

    @Override
    public boolean canDeleteShopHere(@NotNull Player player, @NotNull Location location) {
        return false;
    }

    @Override
    public void load() {
        this.createFlag = new BooleanFlag("quickshop-create");
        this.tradeFlag = new BooleanFlag("quickshop-trade");
        Flags.registerFlag(this.createFlag);
        Flags.registerFlag(this.tradeFlag);
        plugin.getLogger().info(ChatColor.GREEN + getName() + " flags register successfully.");
        this.registerListener();
        Util.debugLog("Success register " + getName() + " flags.");
    }

    @Override
    public void unload() {
        this.unregisterListener();
    }

    private List<Shop> getShops(Plot plot) {
        List<Shop> shopsList = new ArrayList<>();
        for (CuboidRegion region : plot.getRegions()) {
            for (int x = region.getMinimumPoint().getX() >> 4;
                 x <= region.getMaximumPoint().getX() >> 4; x++) {
                for (int z = region.getMinimumPoint().getZ() >> 4;
                     z <= region.getMaximumPoint().getZ() >> 4; z++) {
                    Map<Location, Shop> shops = plugin.getShopManager().getShops(plot.getWorldName(), x, z);
                    if (shops != null) {
                        shopsList.addAll(shops.values());
                    }
                }
            }
        }
        return shopsList;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlotDelete(PlotDeleteEvent event) {
        getShops(event.getPlot()).forEach(Shop::delete);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlotUntrusted(PlayerPlotTrustedEvent event) {
        if (!deleteUntrusted) {
            return;
        }
        if (event.wasAdded()) {
            return; // We only check untrusted
        }
        getShops(event.getPlot()).stream().filter(shop -> shop.getOwner().equals(event.getPlayer())).forEach(Shop::delete);
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() throws Exception {
        init();
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}
