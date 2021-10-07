/*
 * This file is a part of project QuickShop, the name is ServiceInjector.java
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

package org.maxgamer.quickshop;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.database.AbstractDatabaseCore;
import org.maxgamer.quickshop.api.economy.EconomyCore;
import org.maxgamer.quickshop.localization.game.game.GameLanguage;
import org.maxgamer.quickshop.api.shop.ItemMatcher;

/**
 * ServiceInjector used for "Replaceable Modules" features that allow 3rd party QuickShop addon
 * replace some modules used in QuickShop internal by register as service.
 *
 * @author Ghost_chu
 */
public class ServiceInjector {
    public static @NotNull EconomyCore getEconomyCore(@NotNull EconomyCore def) {
        @Nullable RegisteredServiceProvider<? extends EconomyCore> registeredServiceProvider =
                Bukkit.getServicesManager().getRegistration(EconomyCore.class);
        if (registeredServiceProvider == null) {
            return def;
        } else {
            return registeredServiceProvider.getProvider();
        }
    }

    public static @NotNull ItemMatcher getItemMatcher(@NotNull ItemMatcher def) {
        @Nullable RegisteredServiceProvider<? extends ItemMatcher> registeredServiceProvider =
                Bukkit.getServicesManager().getRegistration(ItemMatcher.class);
        if (registeredServiceProvider == null) {
            return def;
        } else {
            return registeredServiceProvider.getProvider();
        }
    }

    public static @NotNull GameLanguage getGameLanguage(@NotNull GameLanguage def) {
        @Nullable RegisteredServiceProvider<? extends GameLanguage> registeredServiceProvider =
                Bukkit.getServicesManager().getRegistration(GameLanguage.class);
        if (registeredServiceProvider == null) {
            return def;
        } else {
            return registeredServiceProvider.getProvider();
        }
    }

    public static @NotNull AbstractDatabaseCore getDatabaseCore(@NotNull AbstractDatabaseCore def) {
        @Nullable RegisteredServiceProvider<? extends AbstractDatabaseCore> registeredServiceProvider =
                Bukkit.getServicesManager().getRegistration(AbstractDatabaseCore.class);
        if (registeredServiceProvider == null) {
            return def;
        } else {
            return registeredServiceProvider.getProvider();
        }
    }
}
