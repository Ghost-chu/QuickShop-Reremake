/*
 * This file is a part of project QuickShop, the name is Economy_Vault.java
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

package org.maxgamer.quickshop.Economy;

import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

public class Economy_Vault implements EconomyCore, Listener {

  private final QuickShop plugin = QuickShop.instance;

  @Getter @Setter @Nullable private Economy vault;

  public Economy_Vault() {
    setupEconomy();
  }

  public boolean checkValid() {
    if (this.vault == null) {
      Bukkit.getPluginManager().disablePlugin(plugin);
      plugin.getLogger().severe("FATAL: Economy system not ready.");
      return false;
    } else {
      return true;
    }
  }

  private String formatInternal(double balance) {
    if (!checkValid()) {
      return "Error";
    }
    try {
      return QuickShop.instance.getConfig().getString("shop.alternate-currency-symbol") + balance;
    } catch (Exception e) {
      return String.valueOf('$' + balance);
    }
  }

  @EventHandler
  public void onServiceRegister(ServiceRegisterEvent event) {
    if (!(event.getProvider() instanceof Economy)) {
      return;
    }
    setupEconomy();
  }

  @EventHandler
  public void onServiceRegister(ServiceUnregisterEvent event) {
    if (!(event.getProvider() instanceof Economy)) {
      return;
    }
    setupEconomy();
  }

  private boolean setupEconomy() {
    RegisteredServiceProvider<Economy> economyProvider;
    try {
      economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
    } catch (Throwable e) {
      return false;
    }

    if (economyProvider != null) {
      this.vault = economyProvider.getProvider();
    }

    if (this.vault == null) {
      return false;
    }

    if (this.vault.getName() == null || this.vault.getName().isEmpty()) {
      plugin
          .getLogger()
          .warning(
              "Current economy plugin not correct process all request, this usually cause by irregular code, you should report this issue to your economy plugin author or use other economy plugin.");
      plugin
          .getLogger()
          .warning(
              "This is technical information, please send this to economy plugin author: "
                  + "VaultEconomyProvider.getName() return a null or empty.");
    } else {
      plugin.getLogger().info("Using economy system: " + this.vault.getName());
    }
    Bukkit.getPluginManager().registerEvents(this, plugin);
    Util.debugLog("Economy service listener was registered.");
    return true;
  }

  public String getProviderName() {
    if (this.vault == null) {
      return "Provider not found.";
    }
    return String.valueOf(this.vault.getName());
  }

  @Override
  public boolean isValid() {
    return this.vault != null;
  }

  @Override
  public String format(double balance) {
    if (!checkValid()) {
      return "Error";
    }
    try {
      String formatedBalance = Objects.requireNonNull(this.vault).format(balance);
      if (formatedBalance == null) // Stupid Ecosystem
      {
        return formatInternal(balance);
      }
      return formatedBalance;
    } catch (Exception e) {
      return formatInternal(balance);
    }
  }

  @Override
  public boolean deposit(@NotNull UUID name, double amount) {
    if (!checkValid()) {
      return false;
    }
    OfflinePlayer p = Bukkit.getOfflinePlayer(name);
    try {
      return Objects.requireNonNull(this.vault).depositPlayer(p, amount).transactionSuccess();
    } catch (Throwable t) {
      plugin.getSentryErrorReporter().ignoreThrow();
      t.printStackTrace();
      plugin
          .getLogger()
          .warning(
              "This seems not QuickShop fault, you should cotact with your economy plugin author. ("
                  + getProviderName()
                  + ")");
      return false;
    }
  }

  @Override
  public boolean withdraw(@NotNull UUID name, double amount) {
    if (!checkValid()) {
      return false;
    }
    OfflinePlayer p = Bukkit.getOfflinePlayer(name);
    try {
      if (!plugin.getConfig().getBoolean("shop.allow-economy-loan")) {
        if (getBalance(name) < amount) {
          return false;
        }
      }
      return Objects.requireNonNull(this.vault).withdrawPlayer(p, amount).transactionSuccess();
    } catch (Throwable t) {
      plugin.getSentryErrorReporter().ignoreThrow();
      t.printStackTrace();
      plugin
          .getLogger()
          .warning(
              "This seems not QuickShop fault, you should cotact with your economy plugin author. ("
                  + getProviderName()
                  + ")");
      return false;
    }
  }

  @Override
  public boolean transfer(@NotNull UUID from, @NotNull UUID to, double amount) {
    if (!checkValid()) {
      return false;
    }
    OfflinePlayer pFrom = Bukkit.getOfflinePlayer(from);
    OfflinePlayer pTo = Bukkit.getOfflinePlayer(to);
    try {
      if (Objects.requireNonNull(this.vault).getBalance(pFrom) >= amount) {
        if (this.vault.withdrawPlayer(pFrom, amount).transactionSuccess()) {
          if (!this.vault.depositPlayer(pTo, amount).transactionSuccess()) {
            this.vault.depositPlayer(pFrom, amount);
            return false;
          }
          return true;
        }
        return false;
      }
      return false;
    } catch (Throwable t) {
      plugin.getSentryErrorReporter().ignoreThrow();
      t.printStackTrace();
      plugin
          .getLogger()
          .warning(
              "This seems not QuickShop fault, you should cotact with your economy plugin author. ("
                  + getProviderName()
                  + ")");
      return false;
    }
  }

  @Override
  public double getBalance(@NotNull UUID name) {
    if (!checkValid()) {
      return 0.0;
    }

    final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);

    if (offlinePlayer.getName() == null) {
      return 0.0;
    }

    try {
      return Objects.requireNonNull(this.vault).getBalance(offlinePlayer);
    } catch (Throwable t) {
      plugin.getSentryErrorReporter().ignoreThrow();
      t.printStackTrace();
      plugin
          .getLogger()
          .warning(
              "This seems not QuickShop fault, you should contact with your economy plugin author. ("
                  + getProviderName()
                  + ")");
      return 0.0;
    }
  }
}
