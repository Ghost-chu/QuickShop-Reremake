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

package org.maxgamer.quickshop.Util;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.PluginsIntegration.IntegrateStage;
import org.maxgamer.quickshop.PluginsIntegration.IntegratedPlugin;
import org.maxgamer.quickshop.PluginsIntegration.IntegrationStage;

import java.util.HashSet;
import java.util.Set;

@Getter
public class IntegrationHelper {
    private Set<IntegratedPlugin> integrations = new HashSet<>();

    public void register(@NotNull IntegratedPlugin clazz) {
        if (!isIntegrationClass(clazz)) {
            throw new InvaildIntegratedPluginClass();
        }
        Util.debugLogHeavy("Registering " + clazz.getName());
        integrations.add(clazz);
    }

    public void unregister(@NotNull IntegratedPlugin clazz) {
        if (!isIntegrationClass(clazz)) {
            throw new InvaildIntegratedPluginClass();
        }
        Util.debugLogHeavy("Unregistering " + clazz.getName());
        integrations.remove(clazz);
    }

    public void callIntegrationsLoad(@NotNull IntegrateStage stage) {
        integrations.forEach(
                integratedPlugin -> {
                    if (integratedPlugin.getClass().getDeclaredAnnotation(IntegrationStage.class).loadStage()
                            == stage) {
                        Util.debugLogHeavy("Calling for load " + integratedPlugin.getName());
                        integratedPlugin.load();
                    } else {
                        Util.debugLogHeavy(
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
                    if (integratedPlugin.getClass().getDeclaredAnnotation(IntegrationStage.class).loadStage()
                            == stage) {
                        Util.debugLogHeavy("Calling for unload " + integratedPlugin.getName());
                        integratedPlugin.unload();
                    } else {
                        Util.debugLogHeavy(
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

    public boolean callIntegrationsCanCreate(@NotNull Player player, @NotNull Location location) {
        for (IntegratedPlugin plugin : integrations) {
            if (!plugin.canCreateShopHere(player, location)) {
                Util.debugLogHeavy("Cancelled by " + plugin.getName());
                return false;
            }
        }
        return true;
    }

    public boolean callIntegrationsCanTrade(@NotNull Player player, @NotNull Location location) {
        for (IntegratedPlugin plugin : integrations) {
            if (!plugin.canTradeShopHere(player, location)) {
                Util.debugLogHeavy("Cancelled by " + plugin.getName());
                return false;
            }
        }
        return true;
    }

    private boolean isIntegrationClass(@NotNull IntegratedPlugin clazz) {
        return clazz.getClass().getDeclaredAnnotation(IntegrationStage.class) != null;
    }
}

class InvaildIntegratedPluginClass extends IllegalArgumentException {
}
