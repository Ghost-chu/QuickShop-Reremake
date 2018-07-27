package org.maxgamer.quickshop.Economy;

import java.util.UUID;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Economy_Vault implements EconomyCore {
	private Economy vault;

	public Economy_Vault() {
		setupEconomy();
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null) {
			this.vault = ((Economy) economyProvider.getProvider());
		}
		return this.vault != null;
	}

	@Override
	public boolean isValid() {
		return this.vault != null;
	}


	@Override
	public String format(double balance) {
		try {
			return this.vault.format(balance);
		} catch (NumberFormatException e) {
		}
		return "$" + balance;
	}

	@Override
	public boolean deposit(UUID name, double amount) {
		OfflinePlayer p = Bukkit.getOfflinePlayer(name);
		return this.vault.depositPlayer(p, amount).transactionSuccess();
	}

	@Override
	public boolean withdraw(UUID name, double amount) {
		OfflinePlayer p = Bukkit.getOfflinePlayer(name);
		return this.vault.withdrawPlayer(p, amount).transactionSuccess();
	}

	@Override
	public boolean transfer(UUID from, UUID to, double amount) {
		OfflinePlayer pFrom = Bukkit.getOfflinePlayer(from);
		OfflinePlayer pTo = Bukkit.getOfflinePlayer(to);
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
	}

	@Override
	public double getBalance(UUID name) {
		OfflinePlayer p = Bukkit.getOfflinePlayer(name);
		return this.vault.getBalance(p);
	}
}