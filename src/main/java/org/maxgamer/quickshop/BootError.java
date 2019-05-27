package org.maxgamer.quickshop;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class BootError {
	
	private String[] errors;
	
	 BootError(String... errors) {
		this.errors=errors;
	}
	
	public String[] getErrors() {
		return errors;
	}
	
	public void printErrors(CommandSender sender) {
		sender.sendMessage(ChatColor.RED+"#####################################################");
		sender.sendMessage(ChatColor.RED+" QuickShop is disabled, Please fix errors and restart");
		for (String issue : errors){
			sender.sendMessage(ChatColor.YELLOW+" "+issue);
		}
		//sender.sendMessage(ChatColor.YELLOW+" "+errors);
		sender.sendMessage(ChatColor.RED+"#####################################################");
		
	}
	

}
