/*
 * This file is a part of project QuickShop, the name is CompatibilityManager.java
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

package org.maxgamer.quickshop.util.compatibility;

import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.holder.QuickShopInstanceHolder;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class CompatibilityManager extends QuickShopInstanceHolder {
    private static final Map<String, Class<? extends QSCompatibilityModule>> compatibilityModuleNameMap = new HashMap<>(2);

    static {
        compatibilityModuleNameMap.put("NoCheatPlus", NCPCompatibilityModule.class);
        compatibilityModuleNameMap.put("Spartan", SpartanCompatibilityModule.class);
    }

    private final Map<String, QSCompatibilityModule> registeredModules = new HashMap<>(5);

    public CompatibilityManager(QuickShop plugin) {
        super(plugin);
    }

    public void searchAndRegisterPlugins() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        for (String pluginName : compatibilityModuleNameMap.keySet()) {
            if (pluginManager.isPluginEnabled(pluginName)) {
                register(pluginName);
            }
        }
    }

    /**
     * Switch the compatibility mode on or off, set false to disable all we known incompatiable plugin
     * listener, set true to enable back all disabled plugin liseners. WIP
     *
     * @param status true=turn on closed listeners, false=turn off all turned on listeners.
     * @param player The player to check the listeners
     */
    public void toggleProtectionListeners(boolean status, @NotNull Player player) {
        for (QSCompatibilityModule module : this.registeredModules.values()) {
            try {
                module.toggle(player, status);
            } catch (Throwable e) {
                unregister(module);
                Util.debugLog("Unregistered module " + module.getName() + " for an error: " + e.getMessage());
            }
        }
    }

    public void unregisterAll() {
        registeredModules.clear();
    }

    public void register(@NotNull QSCompatibilityModule module) {
        registeredModules.put(module.getName(), module);
    }

    public void register(@NotNull String moduleName) {
        QSCompatibilityModule compatibilityModule;
        try {
            compatibilityModule = compatibilityModuleNameMap.get(moduleName).getConstructor(plugin.getClass()).newInstance(plugin);
        } catch (NullPointerException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Invaild compatibility module name: " + moduleName);
        }
        register(compatibilityModule);
    }

    public void unregister(@NotNull String moduleName) {
        registeredModules.remove(moduleName);
    }

    public void unregister(@NotNull QSCompatibilityModule module) {
        registeredModules.remove(module.getName());
    }
}
