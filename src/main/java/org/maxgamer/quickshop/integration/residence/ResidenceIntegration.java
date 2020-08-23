/*
 * This file is a part of project QuickShop, the name is ResidenceIntegration.java
 *  Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
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

package org.maxgamer.quickshop.integration.residence;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.integration.IntegrateStage;
import org.maxgamer.quickshop.integration.IntegratedPlugin;
import org.maxgamer.quickshop.integration.IntegrationStage;

import java.util.List;
import java.util.Map;

@SuppressWarnings("DuplicatedCode")
@IntegrationStage(loadStage = IntegrateStage.onEnableAfter)
public class ResidenceIntegration implements IntegratedPlugin {
    private static final String createFlag = "quickshop-create";
    private static final String tradeFlag = "quickshop-trade";
    private final List<String> createLimits;
    private final List<String> tradeLimits;
    private final boolean whiteList;

    public ResidenceIntegration(QuickShop plugin) {
        this.whiteList = plugin.getConfig().getBoolean("integration.residence.whitelist-mode");
        this.createLimits = plugin.getConfig().getStringList("integration.residence.create");
        this.tradeLimits = plugin.getConfig().getStringList("integration.residence.trade");
    }

    @Override
    public @NotNull String getName() {
        return "Residence";
    }

    private boolean playerHas(FlagPermissions permissions, Player player, String name, boolean def) {
        Flags internalFlag = Flags.getFlag(name);
        if (internalFlag == null) {
            Map<String, Boolean> permPlayerMap = permissions.getPlayerFlags(player.getName());
            Map<String, Boolean> permGlobalMap = permissions.getFlags();
            if (permPlayerMap != null) {
                return permPlayerMap.getOrDefault(name, permGlobalMap.getOrDefault(name, def));
            } else {
                return permGlobalMap.getOrDefault(name, def);
            }
        } else {
            return permissions.playerHas(player, internalFlag, def);
        }
    }

    @Override
    public boolean canCreateShopHere(@NotNull Player player, @NotNull Location location) {
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(location);
        for (String limit : this.createLimits) {
            if ("FLAG".equalsIgnoreCase(limit)) {
                if (residence == null) {
                    // Check world permission
                    if (!playerHas(Residence.getInstance()
                                    .getWorldFlags()
                                    .getPerms(location.getWorld().getName())
                            , player, createFlag, !whiteList)) {
                        return false;
                    }
                } else {
                    if (!playerHas(residence
                                    .getPermissions()
                            , player, createFlag, false)) {
                        return false;
                    }
                }
            } else {
                // Not flag
                if (residence == null) {
                    if (!playerHas(Residence.getInstance()
                                    .getWorldFlags()
                                    .getPerms(location.getWorld().getName())
                            , player, limit, !whiteList)) {
                        return false;
                    }
                } else {
                    if (!playerHas(residence.getPermissions(), player, limit, false)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean canTradeShopHere(@NotNull Player player, @NotNull Location location) {
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(location);

        for (String limit : this.tradeLimits) {
            if ("FLAG".equalsIgnoreCase(limit)) {
                if (residence == null) {
                    // Check world permission
                    if (!playerHas(Residence.getInstance()
                                    .getWorldFlags()
                                    .getPerms(location.getWorld().getName())
                            , player, tradeFlag, !whiteList)) {
                        return false;
                    }
                } else {
                    if (!playerHas(residence
                                    .getPermissions()
                            , player, tradeFlag, true)) {
                        return false;
                    }
                }
            } else {
                // Not flag
                if (residence == null) {
                    if (!playerHas(Residence.getInstance()
                                    .getWorldFlags()
                                    .getPerms(location.getWorld().getName())
                            , player, limit, !whiteList)) {
                        return false;
                    }
                } else {
                    if (!playerHas(residence.getPermissions(), player, limit, false)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void load() {
        FlagPermissions.addFlag(createFlag);
        FlagPermissions.addFlag(tradeFlag);
    }

    @Override
    public void unload() {
    }

}
