/*
 * This file is a part of project QuickShop, the name is PlotSquaredIntegration.java
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

package org.maxgamer.quickshop.PluginsIntegration.PlotSquared;

import com.github.intellectualsites.plotsquared.api.PlotAPI;
import com.github.intellectualsites.plotsquared.plot.flag.BooleanFlag;
import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.PluginsIntegration.IntegrateStage;
import org.maxgamer.quickshop.PluginsIntegration.IntegratedPlugin;
import org.maxgamer.quickshop.PluginsIntegration.IntegrationStage;
import org.maxgamer.quickshop.QuickShop;

@SuppressWarnings("DuplicatedCode")
@IntegrationStage(loadStage = IntegrateStage.onEnableAfter)
public class PlotSquaredIntegration implements IntegratedPlugin {
    PlotAPI plotAPI;
    BooleanFlag createFlag;
    BooleanFlag tradeFlag;
    public PlotSquaredIntegration(QuickShop plugin){
        this.plotAPI = new PlotAPI();
    }
    @Override
    public @NotNull String getName() {
        return "PlotSquared";
    }

    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
        com.github.intellectualsites.plotsquared.plot.object.Location pLocation = new com.github.intellectualsites.plotsquared.plot.object.Location(location.getWorld().getName(),location.getBlockX(),location.getBlockY(),location.getBlockZ());
        Plot plot = pLocation.getPlot();
        return this.createFlag.isTrue(plot);
    }

    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        com.github.intellectualsites.plotsquared.plot.object.Location pLocation = new com.github.intellectualsites.plotsquared.plot.object.Location(location.getWorld().getName(),location.getBlockX(),location.getBlockY(),location.getBlockZ());
        Plot plot = pLocation.getPlot();
        return this.tradeFlag.isFalse(plot);
    }

    @Override
    public void load() {
        this.createFlag = new BooleanFlag("quickshop.create");
        this.tradeFlag = new BooleanFlag("quickshop.trade");
        Flags.registerFlag(this.createFlag);
        Flags.registerFlag(this.tradeFlag);
    }

    @Override
    public void unload() {

    }
}
