/*
 * This file is a part of project QuickShop, the name is EconomyTransactionTest.java
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

package org.maxgamer.quickshop.economy;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EconomyTransactionTest {


    static EconomyCore economy = new TestEconomy();
    static UUID taxAccount = UUID.randomUUID();

    static {
        economy.getBalance(taxAccount);
    }

    private static EconomyTransaction genTransaction(UUID from, UUID to, double amount, double taxModifier, boolean allowLoan) {
        return EconomyTransaction.builder().core(economy).from(from).to(to).amount(amount).taxAccount(taxAccount).taxModifier(taxModifier).allowLoan(allowLoan).build();
    }

    @Test
    public void testTransaction() {
        List<UUID> UUIDList = Stream.generate(UUID::randomUUID).limit(20).collect(Collectors.toList());
        for (UUID account : UUIDList) {
            genTransaction(null, account, 1000, 0.06, false).commit(new EconomyTransaction.TransactionCallback() {
                @Override
                public void onSuccess(@NotNull EconomyTransaction economyTransaction) {

                }

                @Override
                public void onFailed(@NotNull EconomyTransaction economyTransaction) {
                    throw new RuntimeException("Deposit Test Failed");
                }
            });
        }
        assertEquals(20 * 1000 * 0.06D, economy.getBalance(taxAccount));

        assertEquals(1000 * 0.94D, economy.getBalance(UUIDList.get(0)));

        genTransaction(UUIDList.get(5), null, 1000, 0.0, true).commit(new EconomyTransaction.TransactionCallback() {
            @Override
            public void onSuccess(@NotNull EconomyTransaction economyTransaction) {
                assertEquals(-1000 * 0.06D, economy.getBalance(economyTransaction.getFrom()));
            }

            @Override
            public void onFailed(@NotNull EconomyTransaction economyTransaction) {
                throw new RuntimeException("Loan Test Failed");
            }
        });

        genTransaction(UUIDList.get(4), UUIDList.get(5), 1000, 0.06, true).commit(new EconomyTransaction.TransactionCallback() {
            @Override
            public void onSuccess(@NotNull EconomyTransaction economyTransaction) {
                assertEquals(-1000 * 0.06D, economy.getBalance(economyTransaction.getFrom()));
                assertEquals(-1000 * 0.06D + 1000 * 0.94D, economy.getBalance(economyTransaction.getTo()));
                assertEquals(20 * 1000 * 0.06D + (1000 * 0.06D), economy.getBalance(taxAccount));
            }

            @Override
            public void onFailed(@NotNull EconomyTransaction economyTransaction) {
                throw new RuntimeException("Transfer Test Failed");
            }
        });
    }

    @Test
    public void testNull() {
        try {
            EconomyTransaction.builder().core(economy).from(null).to(null).amount(100).core(economy).taxAccount(taxAccount).taxModifier(0.0).build().failSafeCommit();
        } catch (IllegalArgumentException ignored) {
            return;
        }
        throw new RuntimeException("Null Test Failed!");
    }

    static class TestEconomy implements EconomyCore {

        Map<UUID, Double> playerBalanceMap = new HashMap<>(10);

        private Double getOrCreateAccount(UUID uuid) {
            if (!playerBalanceMap.containsKey(uuid)) {
                playerBalanceMap.put(uuid, 0.0);
                return 0.0;
            }
            return playerBalanceMap.get(uuid);
        }

        @Override
        public boolean deposit(UUID name, double amount) {
            playerBalanceMap.put(name, amount + getBalance(name));
            return true;
        }

        @Override
        public String format(double balance) {
            return Double.toString(balance);
        }

        @Override
        public double getBalance(UUID name) {
            return getOrCreateAccount(name);
        }

        @Override
        public boolean transfer(UUID from, UUID to, double amount) {
            double formBalance = getBalance(from);
            playerBalanceMap.put(from, 0.0);
            playerBalanceMap.put(to, getBalance(from) + formBalance);
            return true;
        }

        @Override
        public boolean withdraw(UUID name, double amount) {
            playerBalanceMap.put(name, getBalance(name) - amount);
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public @NotNull String getName() {
            return "TestEconomy";
        }

        @Override
        public @NotNull Plugin getPlugin() {
            throw new UnsupportedOperationException();
        }
    }
}