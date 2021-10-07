/*
 * This file is a part of project QuickShop, the name is Economy.java
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

package org.maxgamer.quickshop.api.economy;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;
import org.maxgamer.quickshop.util.reload.Reloadable;

import java.util.UUID;

public abstract class AbstractEconomy implements EconomyCore, Reloadable {

    private final QuickShop plugin;

    public AbstractEconomy(@NotNull QuickShop plugin) {
        this.plugin = plugin;

    }

    public static EconomyType getNowUsing() {
        return EconomyType.fromID(QuickShop.getInstance().getConfig().getInt("economy-type"));
    }

    @Override
    public abstract String toString();
    //    return core.getClass().getName().split("_")[1];
    //}


    @Override
    public boolean transfer(@NotNull UUID from, @NotNull UUID to, double amount, @NotNull World world, @Nullable String currency) {
        if (!isValid()) {
            return false;
        }
        if (this.getBalance(from, world, currency) >= amount) {
            if (this.withdraw(from, amount, world, currency)) {
                if (this.deposit(to, amount, world, currency)) {
                    this.deposit(from, amount, world, currency);
                    return false;
                }
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public abstract boolean deposit(@NotNull UUID name, double amount, @NotNull World world, @Nullable String currency);

    @Override
    public abstract boolean deposit(@NotNull OfflinePlayer trader, double amount, @NotNull World world, @Nullable String currency);

    /**
     * Formats the given number... E.g. 50.5 becomes $50.5 Dollars, or 50 Dollars 5 Cents
     *
     * @param balance The given number
     * @return The balance in human readable text.
     */
    @Override
    public abstract String format(double balance, @NotNull World world, @Nullable String currency);
    //    return Util.parseColours(core.format(balance, world, currency));
    //    // Fix color issue from some stupid economy plugin....
    //}

    @Override
    public abstract double getBalance(@NotNull UUID name, @NotNull World world, @Nullable String currency);

    @Override
    public abstract double getBalance(@NotNull OfflinePlayer player, @NotNull World world, @Nullable String currency);

    @Override
    public abstract boolean withdraw(@NotNull UUID name, double amount, @NotNull World world, @Nullable String currency);

    @Override
    public abstract boolean withdraw(@NotNull OfflinePlayer trader, double amount, @NotNull World world, @Nullable String currency);

    /**
     * Gets the currency does exists
     *
     * @param currency Currency name
     * @return exists
     */
    @Override
    public abstract boolean hasCurrency(@NotNull World world, @NotNull String currency);

    /**
     * Gets currency supports status
     *
     * @return true if supports
     */
    @Override
    public abstract boolean supportCurrency();

    /**
     * Checks that this economy is valid. Returns false if it is not valid.
     *
     * @return True if this economy will work, false if it will not.
     */
    @Override
    public abstract boolean isValid();

    @Override
    public @NotNull String getName() {
        return "BuiltIn-Economy Processor";
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return plugin;
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() {
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}
