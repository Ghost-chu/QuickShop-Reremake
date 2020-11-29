/*
 * This file is a part of project QuickShop, the name is IntegrationHelper.java
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

package org.maxgamer.quickshop.integration;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.integration.factionsuuid.FactionsUUIDIntegration;
import org.maxgamer.quickshop.integration.griefprevention.GriefPreventionIntegration;
import org.maxgamer.quickshop.integration.lands.LandsIntegration;
import org.maxgamer.quickshop.integration.plotsquared.PlotSquaredIntegrationProxy;
import org.maxgamer.quickshop.integration.residence.ResidenceIntegration;
import org.maxgamer.quickshop.integration.towny.TownyIntegration;
import org.maxgamer.quickshop.integration.worldguard.WorldGuardIntegration;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.holder.QuickShopInstanceHolder;
import org.maxgamer.quickshop.util.holder.Result;

import java.lang.reflect.InvocationTargetException;
import java.util.*;


public class IntegrationHelper extends QuickShopInstanceHolder {
    @Getter
    private static final Map<String, Class<? extends IntegratedPlugin>> integratedPluginNameMap = new HashMap<>(7);

    static {
        integratedPluginNameMap.put("Factions", FactionsUUIDIntegration.class);
        integratedPluginNameMap.put("GriefPrevention", GriefPreventionIntegration.class);
        integratedPluginNameMap.put("Lands", LandsIntegration.class);
        integratedPluginNameMap.put("PlotSquared", PlotSquaredIntegrationProxy.class);
        integratedPluginNameMap.put("Residence", ResidenceIntegration.class);
        integratedPluginNameMap.put("Towny", TownyIntegration.class);
        integratedPluginNameMap.put("WorldGuard", WorldGuardIntegration.class);
    }

    private final Map<String, IntegratedPlugin> integrations = new HashMap<>(7);

    public IntegrationHelper(QuickShop plugin) {
        super(plugin);
    }

    public Map<String, IntegratedPlugin> getIntegrationMap() {
        return Collections.unmodifiableMap(integrations);
    }

    public List<IntegratedPlugin> getIntegrations() {
        return Collections.unmodifiableList(new ArrayList<>(integrations.values()));
    }

    public void searchAndRegisterPlugins() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        for (String pluginName : integratedPluginNameMap.keySet()) {
            if (plugin.getConfig().getBoolean("integration." + pluginName.toLowerCase() + ".enable")) {
                if (pluginManager.isPluginEnabled(pluginName)) {
                    register(pluginName);
                }
            }
        }
    }

    public void register(@NotNull IntegratedPlugin integratedPlugin) {
        if (!isIntegrationClass(integratedPlugin.getClass())) {
            throw new InvalidIntegratedPluginClass("Invaild Integration module: " + integratedPlugin.getName());
        }
        if (!integrations.containsKey(integratedPlugin.getName())) {
            plugin.getLogger().info("Registering " + integratedPlugin.getName() + " integration");
            Util.debugLog("Registering " + integratedPlugin.getName() + " integration");
            integrations.put(integratedPlugin.getName(), integratedPlugin);
        }
    }

    public void register(@NotNull String integratedPluginName) {
        IntegratedPlugin integratedPlugin;
        try {
            integratedPlugin = integratedPluginNameMap.get(integratedPluginName).getConstructor(plugin.getClass()).newInstance(plugin);
        } catch (NullPointerException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new InvalidIntegratedPluginClass("Invaild Integration module name: " + integratedPluginName);
        }
        register(integratedPlugin);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isIntegrationClass(@NotNull Class<?> clazz) {
        return clazz.getDeclaredAnnotation(IntegrationStage.class) != null;
    }

    public void unregister(@NotNull String integratedPluginName) {
        IntegratedPlugin integratedPlugin = integrations.get(integratedPluginName);
        if (integratedPlugin != null) {
            unregister(integratedPlugin);
        }
    }

    public void unregisterAll() {
        for (IntegratedPlugin integratedPlugin : new ArrayList<>(integrations.values())) {
            unregister(integratedPlugin);
        }
    }

    public void unregister(@NotNull IntegratedPlugin integratedPlugin) {
        if (!isIntegrationClass(integratedPlugin.getClass())) {
            throw new InvalidIntegratedPluginClass();
        }
        //Prevent it being removed
        //WorldGuardIntegration will load in onload()V
        //so it won't be registered again when reload
        if (integratedPlugin instanceof WorldGuardIntegration) {
            return;
        }

        plugin.getLogger().info("Unregistering " + integratedPlugin.getName() + " integration");
        Util.debugLog("Unregistering " + integratedPlugin.getName() + " integration");
        integrations.remove(integratedPlugin.getName());
    }

    public void callIntegrationsLoad(@NotNull IntegrateStage stage) {
        integrations.values().forEach(
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
        integrations.values().forEach(
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
        for (IntegratedPlugin plugin : integrations.values()) {
            if (!plugin.canCreateShopHere(player, location)) {
                Util.debugLog("Cancelled by " + plugin.getName());
                return new Result(plugin.getName());
            }
        }
        return Result.SUCCESS;
    }

    public Result callIntegrationsCanTrade(@NotNull Player player, @NotNull Location location) {
        for (IntegratedPlugin plugin : integrations.values()) {
            if (!plugin.canTradeShopHere(player, location)) {
                Util.debugLog("Cancelled by " + plugin.getName());
                return new Result(plugin.getName());
            }
        }
        return Result.SUCCESS;
    }

}

class InvalidIntegratedPluginClass extends IllegalArgumentException {
    public InvalidIntegratedPluginClass() {
        super();
    }

    public InvalidIntegratedPluginClass(String s) {
        super(s);
    }

}
