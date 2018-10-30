package org.maxgamer.quickshop.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.DisplayItem;
import org.maxgamer.quickshop.Util.Util;

public class DisplayProtectionListener implements Listener {
	private QuickShop plugin;

	public DisplayProtectionListener(QuickShop plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		for (int i = 0; i<event.getInventory().getContents().length; i++) {
			try {
				ItemStack is = event.getInventory().getContents()[i];
				if (itemStackCheck(is)) {
					plugin.getLogger().warning("[Exploit alert] "+event.getPlayer().getName()+" had a QuickShop display item on inventory: "+event.getInventory().getType()+":"+event.getInventory().getTitle());
					Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] "+event.getPlayer().getName()+" had a QuickShop display item on inventory: "+event.getInventory().getType()+":"+event.getInventory().getTitle());
					is.setAmount(0);
					is.setType(Material.AIR);
					event.getInventory().clear(i);
				}
			} catch (Exception e) {}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		try {
			if (itemStackCheck(event.getCurrentItem()) || itemStackCheck(event.getCursor())) {
				event.setCancelled(true);
				plugin.getLogger().warning("[Exploit alert] "+event.getWhoClicked().getName()+" had a QuickShop display item on inventory: "+event.getInventory().getType()+":"+event.getInventory().getTitle());
				Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] "+event.getWhoClicked().getName()+" had a QuickShop display item on inventory: "+event.getInventory().getType()+":"+event.getInventory().getTitle());
				event.getCursor().setAmount(0);
				event.getCursor().setType(Material.AIR);
				event.getCurrentItem().setAmount(0);
				event.getCurrentItem().setType(Material.AIR);
				event.setResult(Result.DENY);

			}
		} catch (Exception e) {}		
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryPickupItem(InventoryPickupItemEvent event) {
		try {
			ItemStack is = event.getItem().getItemStack();
			if (itemStackCheck(is)) {
				event.setCancelled(true);
				plugin.getLogger().warning("[Exploit alert] Inventory "+event.getInventory().getTitle()+" at "+event.getItem().getLocation()+" picked up display item "+is);
				Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] Inventory "+event.getInventory().getTitle()+" at "+event.getItem().getLocation()+" picked up display item "+is);
				event.getItem().remove();
				event.getInventory().clear();
			}
		} catch (Exception e) {}
	}

	boolean itemStackCheck(ItemStack is) {
		return DisplayItem.checkShopItem(is);
	}
}
