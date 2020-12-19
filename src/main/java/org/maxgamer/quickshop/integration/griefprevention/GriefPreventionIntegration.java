/*
 * This file is a part of project QuickShop, the name is GriefPreventionIntegration.java
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

package org.maxgamer.quickshop.integration.griefprevention;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.integration.IntegrateStage;
import org.maxgamer.quickshop.integration.IntegrationStage;
import org.maxgamer.quickshop.integration.QSIntegratedPlugin;

import java.util.ArrayList;
import java.util.List;

@IntegrationStage(loadStage = IntegrateStage.onEnableAfter)
public class GriefPreventionIntegration extends QSIntegratedPlugin {
    private final List<Flag> createLimits = new ArrayList<>(3);
    private final List<Flag> tradeLimits = new ArrayList<>(3);
    private final boolean whiteList;
    final GriefPrevention griefPrevention = GriefPrevention.instance;

    public GriefPreventionIntegration(QuickShop plugin) {
        super(plugin);
        ConfigurationSection configurationSection = plugin.getConfig();
        this.whiteList = configurationSection.getBoolean("integration.griefprevention.whitelist-mode");
        createLimits.addAll(toFlags(configurationSection.getStringList("integration.griefprevention.create")));
        tradeLimits.addAll(toFlags(configurationSection.getStringList("integration.griefprevention.trade")));
    }

    private List<Flag> toFlags(List<String> flags) {
        List<Flag> result = new ArrayList<>(3);
        for (String flagStr : flags) {
            Flag flag = Flag.getFlag(flagStr);
            if (flag != null) {
                result.add(flag);
            }
        }
        return result;
    }

    @Override
    public @NotNull String getName() {
        return "GriefPrevention";
    }

    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
        return checkPermission(player, location, createLimits);

    }

    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        return checkPermission(player, location, tradeLimits);
    }

    private boolean checkPermission(@NotNull Player player, @NotNull Location location, List<Flag> limits) {
        if (!griefPrevention.claimsEnabledForWorld(location.getWorld())) {
            return true;
        }
        Claim claim = griefPrevention.dataStore.getClaimAt(location, false, griefPrevention.dataStore.getPlayerData(player.getUniqueId()).lastClaim);
        if (claim == null) {
            return !whiteList;
        }
        for (Flag flag : limits) {
            if (!flag.check(claim, player)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void load() {

    }

    @Override
    public void unload() {

    }

    enum Flag {
        BUILD {
            @Override
            boolean check(Claim claim, Player player) {
                return claim.allowBuild(player, Material.CHEST) == null;
            }
        }, CONTAINER_ACCESS {
            @Override
            boolean check(Claim claim, Player player) {
                return claim.allowContainers(player) == null;
            }
        }, ACCESS {
            @Override
            boolean check(Claim claim, Player player) {
                return claim.allowAccess(player) == null;
            }
        };

        public static Flag getFlag(String flag) {
            flag = flag.toUpperCase();
            for (Flag value : Flag.values()) {
                if (value.name().equals(flag)) {
                    return value;
                }
            }
            return null;
        }

        abstract boolean check(Claim claim, Player player);
    }
}
