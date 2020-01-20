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

package org.maxgamer.quickshop.PluginsIntegration.Towny;

import com.palmergames.bukkit.towny.utils.ShopPlotUtil;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.PluginsIntegration.IntegrateStage;
import org.maxgamer.quickshop.PluginsIntegration.IntegratedPlugin;
import org.maxgamer.quickshop.PluginsIntegration.IntegrationStage;
import org.maxgamer.quickshop.QuickShop;

@SuppressWarnings("DuplicatedCode")
@IntegrationStage(loadStage = IntegrateStage.onEnableAfter)
public class TownyIntegration implements IntegratedPlugin {
  private List<TownyFlags> createFlags;
  private List<TownyFlags> tradeFlags;

  public TownyIntegration(QuickShop plugin) {
    createFlags =
        TownyFlags.deserialize(plugin.getConfig().getStringList("integration.towny.create"));
    tradeFlags =
        TownyFlags.deserialize(plugin.getConfig().getStringList("integration.towny.trade"));
  }

  @Override
  public @NotNull String getName() {
    return "Towny";
  }

  @Override
  public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
    for (TownyFlags flag : createFlags) {
      switch (flag) {
        case OWN:
          if (!ShopPlotUtil.doesPlayerOwnShopPlot(player, location)) {
            return false;
          }
          break;
        case MODIFY:
          if (!ShopPlotUtil.doesPlayerHaveAbilityToEditShopPlot(player, location)) {
            return false;
          }
          break;
        case SHOPTYPE:
          if (!ShopPlotUtil.isShopPlot(location)) {
            return false;
          }
        default:
          // Ignore
      }
    }
    return true;
  }

  @Override
  public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
    for (TownyFlags flag : tradeFlags) {
      switch (flag) {
        case OWN:
          if (!ShopPlotUtil.doesPlayerOwnShopPlot(player, location)) {
            return false;
          }
          break;
        case MODIFY:
          if (!ShopPlotUtil.doesPlayerHaveAbilityToEditShopPlot(player, location)) {
            return false;
          }
          break;
        case SHOPTYPE:
          if (!ShopPlotUtil.isShopPlot(location)) {
            return false;
          }
        default:
          // Ignore
      }
    }
    return true;
  }

  @Override
  public void load() {}

  @Override
  public void unload() {}
}
