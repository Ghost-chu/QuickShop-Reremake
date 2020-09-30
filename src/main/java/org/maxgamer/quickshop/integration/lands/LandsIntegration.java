/*
 * This file is a part of project QuickShop, the name is LandsIntegration.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.integration.lands;

import me.angeschossen.lands.api.events.LandUntrustPlayerEvent;
import me.angeschossen.lands.api.events.PlayerLeaveLandEvent;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.integration.IntegratedPlugin;
import org.maxgamer.quickshop.integration.IntegrationStage;

@IntegrationStage
public class LandsIntegration implements IntegratedPlugin, Listener {

    private final boolean ignoreDisabledWorlds;
    private final boolean whitelist;
    private final me.angeschossen.lands.api.integration.LandsIntegration landsIntegration;
    private final boolean deleteWhenLosePermission;
    private final QuickShop plugin;

    public LandsIntegration(QuickShop plugin) {
        this.plugin = plugin;
        landsIntegration = new me.angeschossen.lands.api.integration.LandsIntegration(plugin);
        ignoreDisabledWorlds = plugin.getConfig().getBoolean("integration.lands.ignore-disabled-worlds");
        whitelist = plugin.getConfig().getBoolean("integration.lands.whitelist-mode");
        deleteWhenLosePermission = plugin.getConfig().getBoolean("integration.lands.delete-on-lose-permission");
    }

    @Override
    public @NotNull String getName() {
        return "Lands";
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
            return !whitelist;
        }
    }

    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
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
        plugin.getShopManager().getAllShops().forEach((shop -> {
            if (event.getLand().hasChunk(shop.getLocation().getWorld(), shop.getLocation().getChunk().getX(), shop.getLocation().getChunk().getZ())) {
                if (event.getTarget().equals(shop.getOwner())) {
                    plugin.log("[UNTRUSTED DELETE] Shop " + shop + " has been deleted due the owner no-longer have permission in land " + event.getLand().getName());
                    shop.delete();
                }
            }
        }));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLandsMember(PlayerLeaveLandEvent event) {
        if (!deleteWhenLosePermission) {
            return;
        }
        plugin.getShopManager().getAllShops().forEach((shop -> {
            if (event.getLand().hasChunk(shop.getLocation().getWorld(), shop.getLocation().getChunk().getX(), shop.getLocation().getChunk().getZ())) {
                if (event.getLandPlayer().getUID().equals(shop.getOwner())) {
                    plugin.log("[UNTRUSTED DELETE] Shop " + shop + " has been deleted due the owner no-longer have permission in land " + event.getLand().getName());
                    shop.delete();
                }
            }
        }));
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
