/*
 * MIT License
 *
 * Copyright © 2020 Bukkit Commons Studio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.maxgamer.quickshop.services.economy;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public interface Economy {
    /**
     * Deposit a given amount of money to a player
     * @param player The target player
     * @param amount The amount of money
     * @return success
     */
    boolean deposit(@NotNull UUID player, double amount);
    /**
     * Withdraw a given amount of money to a player
     * @param player The target player
     * @param amount The amount of money
     * @return success
     */
    boolean withdraw(@NotNull UUID player, double amount);

    /**
     * Check the player has the given amount of money in their wallet.
     * @param player The target player
     * @param amount The amount of money
     * @return The result of has that amount of money
     */
    boolean has(@NotNull UUID player, double amount);

    /**
     * Transfer a given amount of money from FROM player to TO player
     * *README: Impl will try rollback the transaction if failed.*
     * @param from FROM
     * @param to TO
     * @param amount Amount of money
     * @return success
     */
    boolean transfer(@NotNull UUID from, @NotNull UUID to, double amount);

    /**
     * Output the amount of money that player had.
     * @param player Target player
     * @return The balance. If failed, it will return [ - Double.MAX_VALUE ]
     */
    double getBalance(@NotNull UUID player);

    /**
     * Format the balance by economy plugin.
     * @param balance The balance you want to format
     * @return The result of format, if eco plugin failed to format or not impl,
     * it may not present.
     */
    Optional<String> format(double balance);
}
