package org.maxgamer.quickshop.Economy;

import java.util.UUID;

import net.milkbowl.vault.economy.Economy;

import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.maxgamer.quickshop.QuickShop;

public class Economy_Vault implements EconomyCore {
	private Economy vault;
	QuickShop plugin;

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

	@Deprecated
	public boolean deposit(String name, double amount) {
		boolean result = this.vault.depositPlayer(name, amount).transactionSuccess();
		if(QuickShop.debug) {plugin.getLogger().info("Economy debug: Vault return Deprecated deposit result:"+result);}
		return result;
	}

	@Deprecated
	public boolean withdraw(String name, double amount) {
		boolean result = this.vault.withdrawPlayer(name, amount).transactionSuccess();
		if(QuickShop.debug) {plugin.getLogger().info("Economy debug: Vault return Deprecated withdraw result:"+result);}
		return result;
	}

	@Deprecated
	public boolean transfer(String from, String to, double amount) {
		if (this.vault.getBalance(from) >= amount) {
			if (this.vault.withdrawPlayer(from, amount).transactionSuccess()) {
				if (!this.vault.depositPlayer(to, amount).transactionSuccess()) {
					this.vault.depositPlayer(from, amount);
					if(QuickShop.debug) {plugin.getLogger().info("Economy debug:Deprecated transfer failed");}
					return false;
				}
				if(QuickShop.debug) {plugin.getLogger().info("Economy debug:Deprecated transfer success");}
				return true;
			}
			if(QuickShop.debug) {plugin.getLogger().info("Economy debug:Deprecated transfer failed");}
			return false;
		}
		if(QuickShop.debug) {plugin.getLogger().info("Economy debug:Deprecated transfer failed");}
		return false;
	}

	@Deprecated
	public double getBalance(String name) {
		return this.vault.getBalance(name);
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
		boolean result = this.vault.depositPlayer(p, amount).transactionSuccess();
		if(QuickShop.debug) {plugin.getLogger().info("Economy debug:Vault report deposit "+result);}
		return result;
	}

	@Override
	public boolean withdraw(UUID name, double amount) {
		OfflinePlayer p = Bukkit.getOfflinePlayer(name);
		boolean result = this.vault.withdrawPlayer(p, amount).transactionSuccess();
		if(QuickShop.debug) {plugin.getLogger().info("Economy debug:Vault report withdraw "+result);}
		return result;
	}

	@Override
	public boolean transfer(UUID from, UUID to, double amount) {
		OfflinePlayer pFrom = Bukkit.getOfflinePlayer(from);
		OfflinePlayer pTo = Bukkit.getOfflinePlayer(to);
		if (this.vault.getBalance(pFrom) >= amount) {
			if (this.vault.withdrawPlayer(pFrom, amount).transactionSuccess()) {
				if (!this.vault.depositPlayer(pTo, amount).transactionSuccess()) {
					this.vault.depositPlayer(pFrom, amount);
					if(QuickShop.debug) {plugin.getLogger().info("Economy debug:Deprecated transfer failed");}
					return false;
				}
				if(QuickShop.debug) {plugin.getLogger().info("Economy debug:Deprecated transfer success");}
				return true;
			}
			if(QuickShop.debug) {plugin.getLogger().info("Economy debug:Deprecated transfer failed");}
			return false;
		}
		if(QuickShop.debug) {plugin.getLogger().info("Economy debug:Deprecated transfer failed");}
		return false;
	}

	@Override
	public double getBalance(UUID name) {
		OfflinePlayer p = Bukkit.getOfflinePlayer(name);
		return this.vault.getBalance(p);
	}
}