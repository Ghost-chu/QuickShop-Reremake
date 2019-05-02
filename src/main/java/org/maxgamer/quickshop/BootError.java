package org.maxgamer.quickshop;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class BootError {
	
	private String[] errors;
	
	public BootError(String... errors) {
		this.errors=errors;
	}
	
	public String[] getErrors() {
		return errors;
	}
	
	public void printErrors(CommandSender sender) {
		sender.sendMessage(ChatColor.RED+"#########################################################");
		sender.sendMessage(ChatColor.RED+"QuickShop is disabled, please fix problem and try again");
		sender.sendMessage(errors);
		sender.sendMessage(ChatColor.RED+"#########################################################");
		
	}
	

}
