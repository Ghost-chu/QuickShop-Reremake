/*
 * This file is a part of project QuickShop, the name is IntegrationHelper.java
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

package org.maxgamer.quickshop.util;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.integration.IntegrateStage;
import org.maxgamer.quickshop.integration.IntegratedPlugin;
import org.maxgamer.quickshop.integration.IntegrationStage;
import org.maxgamer.quickshop.integration.worldguard.WorldGuardIntegration;
import org.maxgamer.quickshop.util.holder.Result;

import java.util.HashSet;
import java.util.Set;

@Getter
public class IntegrationHelper {
    private final Set<IntegratedPlugin> integrations = new HashSet<>(5);

    public void register(@NotNull IntegratedPlugin clazz) {
        if (!isIntegrationClass(clazz)) {
            throw new InvaildIntegratedPluginClass("Invaild Integration module: " + clazz.getName());
        }
        Util.debugLog("Registering " + clazz.getName());
        integrations.add(clazz);
    }

    private boolean isIntegrationClass(@NotNull IntegratedPlugin clazz) {
        return clazz.getClass().getDeclaredAnnotation(IntegrationStage.class) != null;
    }

    public void unregister(@NotNull IntegratedPlugin clazz) {
        if (!isIntegrationClass(clazz)) {
            throw new InvaildIntegratedPluginClass();
        }
        //Prevent it being removed
        //WorldGuardIntegration will load in onload()V
        //so it won't be registered again when reload
        if (clazz instanceof WorldGuardIntegration) {
            return;
        }
        Util.debugLog("Unregistering " + clazz.getName());
        integrations.remove(clazz);
    }

    public void callIntegrationsLoad(@NotNull IntegrateStage stage) {
        integrations.forEach(
                integratedPlugin -> {
                    if (integratedPlugin.getClass().getDeclaredAnnotation(IntegrationStage.class).loadStage()
                            == stage) {
                        Util.debugLog("Calling for load " + integratedPlugin.getName());
                        integratedPlugin.load();
                    } else {
                        Util.debugLog(
                                "Ignored calling because "
                                        + integratedPlugin.getName()
                                        + " stage is "
                                        + integratedPlugin
                                        .getClass()
                                        .getDeclaredAnnotation(IntegrationStage.class)
                                        .loadStage());
                    }
                });
    }

    public void callIntegrationsUnload(@NotNull IntegrateStage stage) {
        integrations.forEach(
                integratedPlugin -> {
                    if (integratedPlugin.getClass().getDeclaredAnnotation(IntegrationStage.class).unloadStage()
                            == stage) {
                        Util.debugLog("Calling for unload " + integratedPlugin.getName());
                        integratedPlugin.unload();
                    } else {
                        Util.debugLog(
                                "Ignored calling because "
                                        + integratedPlugin.getName()
                                        + " stage is "
                                        + integratedPlugin
                                        .getClass()
                                        .getDeclaredAnnotation(IntegrationStage.class)
                                        .loadStage());
                    }
                });
    }

    public Result callIntegrationsCanCreate(@NotNull Player player, @NotNull Location location) {
        for (IntegratedPlugin plugin : integrations) {
            if (!plugin.canCreateShopHere(player, location)) {
                Util.debugLog("Cancelled by " + plugin.getName());
                return new Result(plugin.getName());
            }
        }
        return Result.SUCCESS;
    }

    public Result callIntegrationsCanTrade(@NotNull Player player, @NotNull Location location) {
        for (IntegratedPlugin plugin : integrations) {
            if (!plugin.canTradeShopHere(player, location)) {
                Util.debugLog("Cancelled by " + plugin.getName());
                return new Result(plugin.getName());
            }
        }
        return Result.SUCCESS;
    }

}

class InvaildIntegratedPluginClass extends IllegalArgumentException {
    public InvaildIntegratedPluginClass() {
        super();
    }

    public InvaildIntegratedPluginClass(String s) {
        super(s);
    }

}
