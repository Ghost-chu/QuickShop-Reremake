/*
 * This file is a part of project QuickShop, the name is LandsIntegration.java
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

package org.maxgamer.quickshop.integration.lands;

import java.util.Map;
import java.util.UUID;
import me.angeschossen.lands.api.events.LandUntrustPlayerEvent;
import me.angeschossen.lands.api.events.PlayerLeaveLandEvent;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.integration.IntegrationStage;
import org.maxgamer.quickshop.integration.QSIntegratedPlugin;
import org.maxgamer.quickshop.shop.Shop;
import org.maxgamer.quickshop.shop.ShopChunk;

@IntegrationStage
public class LandsIntegration extends QSIntegratedPlugin implements Listener {

  private final boolean ignoreDisabledWorlds;
  private final boolean whitelist;
  private final me.angeschossen.lands.api.integration
      .LandsIntegration landsIntegration;
  private final boolean deleteWhenLosePermission;

  public LandsIntegration(QuickShop plugin) {
    super(plugin);
    landsIntegration =
        new me.angeschossen.lands.api.integration.LandsIntegration(plugin);
    ignoreDisabledWorlds = plugin.getConfig().getBoolean(
        "integration.lands.ignore-disabled-worlds");
    whitelist =
        plugin.getConfig().getBoolean("integration.lands.whitelist-mode");
    deleteWhenLosePermission = plugin.getConfig().getBoolean(
        "integration.lands.delete-on-lose-permission");
  }

  @Override
  public @NotNull String getName() {
    return "Lands";
  }

  @Override
  public boolean canCreateShopHere(@NotNull Player player,
                                   @NotNull Location location) {
    if (landsIntegration.getLandWorld(location.getWorld()) == null) {
      return ignoreDisabledWorlds;
    }
    Land land = landsIntegration.getLand(location);
    if (land != null) {
      return land.getOwnerUID().equals(player.getUniqueId()) ||
          land.isTrusted(player.getUniqueId());
    } else {
      return !whitelist;
    }
  }

  @Override
  public boolean canTradeShopHere(@NotNull Player player,
                                  @NotNull Location location) {
    if (landsIntegration.getLandWorld(location.getWorld()) == null) {
      return ignoreDisabledWorlds;
    }
    return true;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onLandsPermissionChanges(LandUntrustPlayerEvent event) {
    if (!deleteWhenLosePermission) {
      return;
    }
    deleteShopInLand(event.getLand(), event.getTarget());
  }

  private void deleteShopInLand(Land land, UUID target) {
    // Getting all shop with world-chunk-shop mapping
    for (Map.Entry<String, Map<ShopChunk, Map<Location, Shop>>> entry :
         plugin.getShopManager().getShops().entrySet()) {
      // Matching world
      World world = Bukkit.getWorld(entry.getKey());
      if (world != null) {
        // Matching chunk
        for (Map.Entry<ShopChunk, Map<Location, Shop>> chunkedShopEntry :
             entry.getValue().entrySet()) {
          ShopChunk shopChunk = chunkedShopEntry.getKey();
          if (land.hasChunk(world, shopChunk.getX(), shopChunk.getZ())) {
            // Matching Owner and delete it
            Map<Location, Shop> shops = chunkedShopEntry.getValue();
            for (Shop shop : shops.values()) {
              if (target.equals(shop.getOwner())) {
                plugin.log(
                    "[UNTRUSTED DELETE] Shop " + shop +
                    " has been deleted due the owner no-longer have permission in land " +
                    land.getName());
                new BukkitRunnable() {
                  @Override
                  public void run() {
                    shop.delete();
                  }
                }.runTask(plugin);
              }
            }
          }
        }
      }
    }
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onLandsMember(PlayerLeaveLandEvent event) {
    if (!deleteWhenLosePermission) {
      return;
    }
    deleteShopInLand(event.getLand(), event.getLandPlayer().getUID());
  }

  @Override
  public void load() {
    Bukkit.getPluginManager().registerEvents(this, plugin);
  }

  @Override
  public void unload() {
    HandlerList.unregisterAll(this);
  }
}
