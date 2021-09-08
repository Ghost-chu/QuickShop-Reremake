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

package org.maxgamer.quickshop.economy;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

import java.util.UUID;

public class Economy implements EconomyCore {

    private final QuickShop plugin;
    @Getter
    @Setter
    @NotNull
    private EconomyCore core;

    public Economy(@NotNull QuickShop plugin, @NotNull EconomyCore core) {
        this.plugin = plugin;
        this.core = core;
    }

    public static EconomyType getNowUsing() {
        return EconomyType.fromID(QuickShop.getInstance().getConfig().getInt("economy-type"));
    }

    @Override
    public String toString() {
        return core.getClass().getName().split("_")[1];
    }

    @Override
    public boolean deposit(@NotNull UUID name, double amount, @NotNull World world, @Nullable String currency) {
        return core.deposit(name, amount, world, currency);
    }

    @Override
    public boolean deposit(@NotNull OfflinePlayer trader, double amount, @NotNull World world, @Nullable String currency) {
        return core.deposit(trader, amount, world, currency);
    }

    /**
     * Formats the given number... E.g. 50.5 becomes $50.5 Dollars, or 50 Dollars 5 Cents
     *
     * @param balance The given number
     * @return The balance in human readable text.
     */
    @Override
    public String format(double balance, @NotNull World world, @Nullable String currency) {
        return Util.parseColours(core.format(balance, world, currency));
        // Fix color issue from some stupid economy plugin....
    }

    @Override
    public double getBalance(@NotNull UUID name, @NotNull World world, @Nullable String currency) {
        return core.getBalance(name, world, currency);
    }

    @Override
    public double getBalance(@NotNull OfflinePlayer player, @NotNull World world, @Nullable String currency) {
        return core.getBalance(player, world, currency);
    }

    @Override
    public boolean transfer(@NotNull UUID from, @NotNull UUID to, double amount, @NotNull World world, @Nullable String currency) {
        return core.transfer(from, to, amount, world, currency);
    }

    @Override
    public boolean withdraw(@NotNull UUID name, double amount, @NotNull World world, @Nullable String currency) {
        return core.withdraw(name, amount, world, currency);
    }

    @Override
    public boolean withdraw(@NotNull OfflinePlayer trader, double amount, @NotNull World world, @Nullable String currency) {
        return core.withdraw(trader, amount, world, currency);
    }

    /**
     * Gets the currency does exists
     *
     * @param currency Currency name
     * @return exists
     */
    @Override
    public boolean hasCurrency(@NotNull World world, @NotNull String currency) {
        return this.core.hasCurrency(world, currency);
    }

    /**
     * Gets currency supports status
     *
     * @return true if supports
     */
    @Override
    public boolean supportCurrency() {
        return this.core.supportCurrency();
    }

    /**
     * Checks that this economy is valid. Returns false if it is not valid.
     *
     * @return True if this economy will work, false if it will not.
     */
    @Override
    public boolean isValid() {
        return core.isValid();
    }

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
    public ReloadResult reloadModule() throws Exception {
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build()
    }
}
