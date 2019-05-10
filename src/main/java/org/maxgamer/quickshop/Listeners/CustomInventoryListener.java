package org.maxgamer.quickshop.Listeners;

import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.Shop.InventoryPreview;

public class CustomInventoryListener implements Listener {
	@EventHandler
	public void invEvent(InventoryInteractEvent e) {
		Inventory inventory = e.getInventory();
		ItemStack[] stacks = inventory.getContents();
		for (ItemStack itemStack : stacks) {
			if(itemStack==null)
				continue;
			if(InventoryPreview.isPreviewItem(itemStack)) {
				e.setCancelled(true);
				e.setResult(Result.DENY);
			}
		}
	}
	@EventHandler
	public void invEvent(InventoryMoveItemEvent e) {
		if(InventoryPreview.isPreviewItem(e.getItem())) {
			e.setCancelled(true);
		}	
	}
	@EventHandler
	public void invEvent(InventoryClickEvent e) {
		if(InventoryPreview.isPreviewItem(e.getCursor())) {
			e.setCancelled(true);
			e.setResult(Result.DENY);
		}
		if(InventoryPreview.isPreviewItem(e.getCurrentItem())) {
			e.setCancelled(true);
			e.setResult(Result.DENY);
		}
	}
	@EventHandler
	public void invEvent(InventoryDragEvent e) {
		if(InventoryPreview.isPreviewItem(e.getCursor())) {
			e.setCancelled(true);
			e.setResult(Result.DENY);
		}
		if(InventoryPreview.isPreviewItem(e.getOldCursor())) {
			e.setCancelled(true);
			e.setResult(Result.DENY);
		}
		
	}
	@EventHandler
	public void invEvent(InventoryPickupItemEvent e) {
		Inventory inventory = e.getInventory();
		ItemStack[] stacks = inventory.getContents();
		for (ItemStack itemStack : stacks) {
			if(itemStack==null)
				continue;
			if(InventoryPreview.isPreviewItem(itemStack)) {
				e.setCancelled(true);
			}
		}
	}
}
