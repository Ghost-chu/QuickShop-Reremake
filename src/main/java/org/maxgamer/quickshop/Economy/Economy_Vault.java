package org.maxgamer.quickshop.Economy;

import java.util.UUID;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.*;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.Util;

public class Economy_Vault implements EconomyCore {
    private Economy vault;
    private QuickShop plugin = QuickShop.instance;
    final private String errorMsg = "QuickShop got an error when calling your Economy system, this is NOT a QuickShop error, please do not report this issue to the QuickShop's Issue tracker, ask your Economy plugin's author.";

    public Economy_Vault() {
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
            this.vault = ((Economy) economyProvider.getProvider());
        }
        return this.vault != null;
    }

    @Override
    public boolean isValid() {
        return this.vault != null;
    }

    @Deprecated
    public boolean deposit(@NotNull String name, double amount) {
        Util.sendDeprecatedMethodWarn();
        return this.vault.depositPlayer(name, amount).transactionSuccess();
    }

    @Deprecated
    public boolean withdraw(@NotNull String name, double amount) {
        Util.sendDeprecatedMethodWarn();
        return this.vault.withdrawPlayer(name, amount).transactionSuccess();
    }

    @Deprecated
    public boolean transfer(@NotNull String from, @NotNull String to, double amount) {
        Util.sendDeprecatedMethodWarn();
        if (this.vault.getBalance(from) >= amount) {
            if (this.vault.withdrawPlayer(from, amount).transactionSuccess()) {
                if (!this.vault.depositPlayer(to, amount).transactionSuccess()) {
                    this.vault.depositPlayer(from, amount);
                    return false;
                }
                return true;
            }
            return false;
        }
        return false;
    }

    @Deprecated
    public double getBalance(@NotNull String name) {
        Util.sendDeprecatedMethodWarn();
        return this.vault.getBalance(name);
    }

    @Override
    public String format(double balance) {
        try {
            String formatedBalance = this.vault.format(balance);
            if (formatedBalance == null)//Stupid Ecosystem
                return formatInternal(balance);
            return formatedBalance;
        } catch (Exception e) {
            return formatInternal(balance);
        }

    }

    private String formatInternal(double balance) {
        try {
            return String.valueOf(QuickShop.instance.getConfig().getString("shop.alternate-currency-symbol") + balance);
        } catch (Exception e) {
            return String.valueOf('$' + balance);
        }
    }

    @Override
    public boolean deposit(@NotNull UUID name, double amount) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(name);
        try {
            return this.vault.depositPlayer(p, amount).transactionSuccess();
        } catch (Throwable t) {
            plugin.getSentryErrorReporter().ignoreThrow();
            t.printStackTrace();
            plugin.getLogger().warning(this.errorMsg);
            return false;
        }
    }

    @Override
    public boolean withdraw(@NotNull UUID name, double amount) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(name);
        try {
            return this.vault.withdrawPlayer(p, amount).transactionSuccess();
        } catch (Throwable t) {
            plugin.getSentryErrorReporter().ignoreThrow();
            t.printStackTrace();
            plugin.getLogger().warning(this.errorMsg);
            return false;
        }
    }

    @Override
    public boolean transfer(@NotNull UUID from, @NotNull UUID to, double amount) {
        OfflinePlayer pFrom = Bukkit.getOfflinePlayer(from);
        OfflinePlayer pTo = Bukkit.getOfflinePlayer(to);
        try {
            if (this.vault.getBalance(pFrom) >= amount) {
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
            plugin.getLogger().warning(this.errorMsg);
            return false;
        }
    }

    @Override
    public double getBalance(@NotNull UUID name) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(name);
        try {
            return this.vault.getBalance(p);
        } catch (Throwable t) {
            plugin.getSentryErrorReporter().ignoreThrow();
            t.printStackTrace();
            plugin.getLogger().warning(this.errorMsg);
            return 0.0;
        }
    }
}