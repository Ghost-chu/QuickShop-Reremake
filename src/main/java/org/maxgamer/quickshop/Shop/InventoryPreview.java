package org.maxgamer.quickshop.Shop;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.maxgamer.quickshop.Util.MsgUtil;

public class InventoryPreview implements Listener {
	
	ItemStack itemStack;
	Inventory inventory;
	Player player;
	
	public InventoryPreview(ItemStack itemStack,Player player) {
		this.itemStack=itemStack.clone();
		this.player=player;
		if(this.itemStack.getItemMeta().hasLore()) {
			ItemMeta itemMeta = this.itemStack.getItemMeta();
			List<String> lores = itemMeta.getLore();
			lores.add("QuickShop GUI preview item");
			itemMeta.setLore(lores);
			this.itemStack.setItemMeta(itemMeta);
		}else {
			ItemMeta itemMeta = this.itemStack.getItemMeta();
			List<String> lores = new ArrayList<String>();
			lores.add("QuickShop GUI preview item");
			itemMeta.setLore(lores);
			this.itemStack.setItemMeta(itemMeta);
		}
	}
	
	public void show() {
		if(inventory!=null) // Not inited
			close();
		if(player==null) // Null pointer exception
			return;
		if(player.isSleeping()) // Bed bug
			return;
		final int size = 9;
		inventory = Bukkit.createInventory(null, size, MsgUtil.getMessage("menu.preview"));
		for (int i = 0; i < size; i++) {
			inventory.setItem(i, itemStack);
		}
		player.openInventory(inventory);
		// Total 9
	}
	
	public void close() {
		if(inventory==null)
			return;
		
		for (HumanEntity player : inventory.getViewers()) {
			player.closeInventory();
		}
		inventory = null; // Destory
	}
	
	public static boolean isPreviewItem(ItemStack stack) {
		List<String> lores = stack.getItemMeta().getLore();
		for (String string : lores) {
			if(string.equals("QuickShop GUI preview item")) {
				return true;
			}
		}
		return false;
	}
}
