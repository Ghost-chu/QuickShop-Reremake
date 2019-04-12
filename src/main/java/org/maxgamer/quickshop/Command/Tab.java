package org.maxgamer.quickshop.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Util.MsgUtil;

import java.util.ArrayList;
import java.util.List;

public class Tab implements TabCompleter {
	QuickShop plugin;

	public Tab(QuickShop plugin) {
		this.plugin = plugin;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		for (int i = 0; i < args.length; i++) {
			args[i] = args[i].toLowerCase();
			//Make all is low case
		}
		ArrayList<String> tabList = new ArrayList<>();
		if(args.length==1) {
			if (sender.hasPermission("quickshop.unlimited"))
				tabList.add("unlimited");
			if (sender.hasPermission("quickshop.setowner"))
				tabList.add("setowner");
			if (sender.hasPermission("quickshop.create.buy"))
				tabList.add("buy");
			if (sender.hasPermission("quickshop.create.sell")) {
				tabList.add("sell");
				tabList.add("create");
			}
			if (sender.hasPermission("quickshop.create.changeprice"))
				tabList.add("price");
			if (sender.hasPermission("quickshop.clean"))
				tabList.add("clean");
			if (sender.hasPermission("quickshop.find"))
				tabList.add("find");
			if (sender.hasPermission("quickshop.refill"))
				tabList.add("refill");
			if (sender.hasPermission("quickshop.empty"))
				tabList.add("empty");
			if (sender.hasPermission("quickshop.fetchmessage"))
				tabList.add("fetchmessage");
			if (sender.hasPermission("quickshop.info"))
				tabList.add("info");
			if (sender.hasPermission("quickshop.debug"))
				tabList.add("debug");
			if (sender.hasPermission("quickshop.paste"))
				tabList.add("paste");
			return tabList;
		}else if(args.length==2) {
			if (args[1].equals("create")&&sender.hasPermission("quickshop.create.sell")) {
				tabList.add(MsgUtil.getMessage("tabcomplete.price"));
			}
			if (args[1].equals("price")&&sender.hasPermission("quickshop.create.changeprice")) {
				tabList.add(MsgUtil.getMessage("tabcomplete.price"));
			}
			if (args[1].equals("find")&&sender.hasPermission("quickshop.find")) {
				tabList.add(MsgUtil.getMessage("tabcomplete.range"));
			}
			if (args[1].equals("refill")&&sender.hasPermission("quickshop.refill")) {
				tabList.add(MsgUtil.getMessage("tabcomplete.amount"));
			}
			return tabList;
		}else {
			return null;
		}
	}
}
