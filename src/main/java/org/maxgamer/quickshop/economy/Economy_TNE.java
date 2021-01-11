/*
 * This file is a part of project QuickShop, the name is Economy_TNE.java
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
import net.tnemc.core.TNE;
import net.tnemc.core.common.api.TNEAPI;
import net.tnemc.core.common.currency.TNECurrency;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * @deprecated Unstable
 */
public class Economy_TNE implements EconomyCore {

    private final QuickShop plugin;
    private final boolean allowLoan;

    @Getter
    @Setter
    @Nullable
    private TNEAPI api;

    public Economy_TNE(@NotNull QuickShop plugin) {
        this.plugin = plugin;
        this.allowLoan = plugin.getConfig().getBoolean("shop.allow-economy-loan");
        setupEconomy();
    }

    private void setupEconomy() {
        this.api = TNE.instance().api();
    }

    @Nullable
    private TNECurrency getCurrency(@Nullable String currency) {
        if (!isValid()) {
            return null;
        }
        if (currency == null) {
            return null;
        }
        for (TNECurrency apiCurrency : this.api.getCurrencies()) {
            if (apiCurrency.getIdentifier().equals(currency)) {
                return apiCurrency;
            }
        }
        return this.api.getCurrencies().iterator().next();
    }

    /**
     * Deposits a given amount of money from thin air to the given username.
     *
     * @param name     The exact (case insensitive) username to give money to
     * @param amount   The amount to give them
     * @param currency The currency name
     * @return True if success (Should be almost always)
     */
    @Override
    public boolean deposit(@NotNull UUID name, double amount, @Nullable String currency) {
        deposit(Bukkit.getOfflinePlayer(name), amount, currency);
        return false;
    }

    /**
     * Deposits a given amount of money from thin air to the given username.
     *
     * @param trader   The player to give money to
     * @param amount   The amount to give them
     * @param currency The currency name
     * @return True if success (Should be almost always)
     */
    @Override
    public boolean deposit(@NotNull OfflinePlayer trader, double amount, @Nullable String currency) {
        if (!isValid()) {
            return false;
        }
        BigDecimal decimal = BigDecimal.valueOf(amount);
        if (!this.api.canAddHoldings(trader.getName(), decimal)) {
            return false;
        }
        return this.api.addHoldings(trader.getName(), decimal, getCurrency(currency));
    }

    /**
     * Formats the given number... E.g. 50.5 becomes $50.5 Dollars, or 50 Dollars 5 Cents
     *
     * @param balance  The given number
     * @param currency The currency name
     * @return The balance in human readable text.
     */
    @Override
    public String format(double balance, @Nullable String currency) {
        if (!isValid()) {
            return null;
        }
        BigDecimal decimal = BigDecimal.valueOf(balance);
        return this.api.format(decimal, getCurrency(currency), TNE.instance().defaultWorld);
    }

    /**
     * Fetches the balance of the given account name
     *
     * @param name     The name of the account
     * @param currency The currency name
     * @return Their current balance.
     */
    @Override
    public double getBalance(@NotNull UUID name, @Nullable String currency) {
        return getBalance(Bukkit.getOfflinePlayer(name), currency);
    }

    /**
     * Fetches the balance of the given player
     *
     * @param player   The name of the account
     * @param currency The currency name
     * @return Their current balance.
     */
    @Override
    public double getBalance(@NotNull OfflinePlayer player, @Nullable String currency) {
        if (!isValid()) {
            return 0.0;
        }
        if (getCurrency(currency) != null) {
            return this.api.getHoldings(player.getName(), getCurrency(currency)).doubleValue();
        } else {
            return this.api.getHoldings(player.getName(), TNE.instance().defaultWorld).doubleValue();
        }
    }

    /**
     * Transfers the given amount of money from Player1 to Player2
     *
     * @param from     The player who is paying money
     * @param to       The player who is receiving money
     * @param amount   The amount to transfer
     * @param currency The currency name
     * @return true if success (Payer had enough cash, receiver was able to receive the funds)
     */
    @Override
    public boolean transfer(@NotNull UUID from, @NotNull UUID to, double amount, @Nullable String currency) {
        return false;
    }

    /**
     * Withdraws a given amount of money from the given username and turns it to thin air.
     *
     * @param name     The exact (case insensitive) username to take money from
     * @param amount   The amount to take from them
     * @param currency The currency name
     * @return True if success, false if they didn't have enough cash
     */
    @Override
    public boolean withdraw(@NotNull UUID name, double amount, @Nullable String currency) {
        return withdraw(Bukkit.getOfflinePlayer(name), amount, currency);
    }

    /**
     * Withdraws a given amount of money from the given username and turns it to thin air.
     *
     * @param trader   The player to take money from
     * @param amount   The amount to take from them
     * @param currency The currency name
     * @return True if success, false if they didn't have enough cash
     */
    @Override
    public boolean withdraw(@NotNull OfflinePlayer trader, double amount, @Nullable String currency) {
        if (!isValid()) {
            return false;
        }
        BigDecimal decimal = BigDecimal.valueOf(amount);
        if (!this.api.canRemoveHoldings(trader.getName(), decimal, getCurrency(currency))) {
            return false;
        }
        return this.api.removeHoldings(trader.getName(), decimal);
    }

    /**
     * Checks that this economy is valid. Returns false if it is not valid.
     *
     * @return True if this economy will work, false if it will not.
     */
    @Override
    public boolean isValid() {
        return this.api != null && TNE.instance() != null;
    }

    @Override
    public @NotNull String getName() {
        return "BuiltIn-TNE-Unstable";
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return this.plugin;
    }
}
