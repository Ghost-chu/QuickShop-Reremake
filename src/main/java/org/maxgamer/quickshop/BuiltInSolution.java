package org.maxgamer.quickshop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class BuiltInSolution {
	public static BootError econError() {
		// Check Vault is installed
		if(Bukkit.getPluginManager().getPlugin("Vault")==null) {
			// Vault not installed
			return new BootError("Vault not installed or loaded!","Make sure you installed Vault.");
		}
		// Vault is installed
		if(Bukkit.getPluginManager().getPlugin("CMI")!=null) {
			// Found may in-compatiable plugin
			return new BootError("No Economy plugin loaded","Make sure you have economy plugin hook into Vault.",ChatColor.YELLOW+"Incompatible detected: CMI Installed","Download CMI Edition Vault may can fix this.");
		}
		return new BootError("No Economy plugin loaded","Install one economy plugin to let Vault working.");
	}
	public static BootError databaseError() {
		return new BootError("Error connecting to database","Make sure your database service is runnning.","Or check the configuration in config.yml");
	}
}
