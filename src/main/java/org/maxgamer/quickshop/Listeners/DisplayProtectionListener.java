package org.maxgamer.quickshop.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event.Result;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.event.player.PlayerMoveEvent;
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
					is.setAmount(0);
					is.setType(Material.AIR);
					event.getPlayer().closeInventory();
					Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. ("+event.getPlayer().getLocation().toString()+")");
					Util.inventoryCheck(event.getInventory());
				}
			} catch (Exception e) {}
		}
	}
	@EventHandler(ignoreCancelled = true)
	public void heldItem (PlayerItemHeldEvent e){
		ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
		ItemStack stackOffHand = e.getPlayer().getInventory().getItemInOffHand();
		try {
			if (DisplayItem.checkShopItem(stack)) {
				e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR,0));
				// You shouldn't be able to pick up that...
				Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. ("+e.getPlayer().getLocation().toString()+")");
				Util.inventoryCheck(e.getPlayer().getInventory());
			}
			if (DisplayItem.checkShopItem(stackOffHand)) {
				e.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.AIR,0));
				// You shouldn't be able to pick up that...
				Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. ("+e.getPlayer().getLocation().toString()+")");
				Util.inventoryCheck(e.getPlayer().getInventory());
			}
		} catch (NullPointerException ex) {

		}
	}
	@EventHandler(ignoreCancelled = true)
	public void mendItem (PlayerItemMendEvent e){
		ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
		ItemStack stackOffHand = e.getPlayer().getInventory().getItemInOffHand();
		try {
			if (DisplayItem.checkShopItem(stack)) {
				e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR,0));
				// You shouldn't be able to pick up that...
				Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. ("+e.getPlayer().getLocation().toString()+")");
				Util.inventoryCheck(e.getPlayer().getInventory());
			}
			if (DisplayItem.checkShopItem(stackOffHand)) {
				e.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.AIR,0));
				// You shouldn't be able to pick up that...
				Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. ("+e.getPlayer().getLocation().toString()+")");
				Util.inventoryCheck(e.getPlayer().getInventory());
			}
		} catch (NullPointerException ex) {
		}
	}
	@EventHandler(ignoreCancelled = true)
	public void changeHand (PlayerChangedMainHandEvent e){
		ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
		ItemStack stackOffHand = e.getPlayer().getInventory().getItemInOffHand();
		try {
			if (DisplayItem.checkShopItem(stack)) {
				e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR,0));
				// You shouldn't be able to pick up that...
				Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. ("+e.getPlayer().getLocation().toString()+")");
				Util.inventoryCheck(e.getPlayer().getInventory());
			}
			if (DisplayItem.checkShopItem(stackOffHand)) {
				e.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.AIR,0));
				// You shouldn't be able to pick up that...
				Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. ("+e.getPlayer().getLocation().toString()+")");
				Util.inventoryCheck(e.getPlayer().getInventory());
			}
		} catch (NullPointerException ex) {
		}

	}
	@EventHandler(ignoreCancelled = true)
	public void onPlayerPickup(EntityPickupItemEvent e) {
		ItemStack stack = e.getItem().getItemStack();
		try {
			if (DisplayItem.checkShopItem(stack)) {
				e.setCancelled(true);
				Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. ("+e.getEntity().getLocation().toString()+")");
				// You shouldn't be able to pick up that...
				e.getItem().remove();
				e.getEntity().setCanPickupItems(false);
				if(e.getEntityType() != EntityType.PLAYER) {
					Util.debugLog("A entity at "+e.getEntity().getLocation().toString()+" named "+e.getEntity().getCustomName()+"("+e.getEntityType().name()+" trying pickup item, already banned this entity item pickup power.");
				}
				
			}
		} catch (NullPointerException ex) {
		} // if meta/displayname/stack is null. We don't really care in that case.
	}
	@EventHandler(ignoreCancelled = true)
	public void onPlayerClick(PlayerInteractEvent e) {
		ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
		ItemStack stackOffHand = e.getPlayer().getInventory().getItemInOffHand();
		try {
			if (DisplayItem.checkShopItem(stack) || DisplayItem.checkShopItem(stackOffHand)) {
				e.setCancelled(true);
				Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. ("+e.getPlayer().getInventory().getLocation().toString()+")");
				Util.inventoryCheck(e.getPlayer().getInventory());
				// You shouldn't be able to pick up that...
			}
		} catch (NullPointerException ex) {
		} // if meta/displayname/stack is null. We don't really care in that case.
	}
	@EventHandler(ignoreCancelled = true)
	public void moveing (PlayerMoveEvent e){
		ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
		ItemStack stackOffHand = e.getPlayer().getInventory().getItemInOffHand();
		try {
			if (DisplayItem.checkShopItem(stack)) {
				e.getPlayer().getInventory().setItemInMainHand(new ItemStack(Material.AIR,0));
				// You shouldn't be able to pick up that...
				plugin.getLogger().warning("[Exploit Alert] A QuickShop item found in "+e.getPlayer().getName()+"'s inventory"+" Deleteing...");
				Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] A QuickShop item found in "+e.getPlayer().getName()+"'s inventory"+" Deleteing...");
				Util.inventoryCheck(e.getPlayer().getInventory());
			}
			if (DisplayItem.checkShopItem(stackOffHand)) {
				e.getPlayer().getInventory().setItemInOffHand(new ItemStack(Material.AIR,0));
				// You shouldn't be able to pick up that...
				Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. ("+e.getPlayer().getLocation().toString()+")");
				Util.inventoryCheck(e.getPlayer().getInventory());
			}
		} catch (NullPointerException ex) {

		}
		 // if meta/displayname/stack is null. We don't really care in that case.
	}
	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		try {
			if (itemStackCheck(event.getCurrentItem()) || itemStackCheck(event.getCursor())) {
				event.setCancelled(true);
				plugin.getLogger().warning("[Exploit alert] "+event.getWhoClicked().getName()+" had a QuickShop display item on inventory: "+event.getInventory().getType()+":"+event.getView().getTitle());
				Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] "+event.getWhoClicked().getName()+" had a QuickShop display item on inventory: "+event.getInventory().getType()+":"+event.getView().getTitle());
				event.getCursor().setAmount(0);
				event.getCursor().setType(Material.AIR);
				event.getCurrentItem().setAmount(0);
				event.getCurrentItem().setType(Material.AIR);
				event.setResult(Result.DENY);
				Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. ("+event.getInventory().getLocation().toString()+")");
				Util.inventoryCheck(event.getInventory());
			}
		} catch (Exception e) {}		
	}

	@EventHandler(ignoreCancelled = true)
	public void onInventoryPickupItem(InventoryPickupItemEvent event) {
		try {
			ItemStack is = event.getItem().getItemStack();
			if (itemStackCheck(is)) {
				event.setCancelled(true);
//				plugin.getLogger().warning("[Exploit alert] Inventory "+event.getInventory().getName()+" at "+event.getItem().getLocation()+" picked up display item "+is);
//				Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] Inventory "+event.getView().getTitle()+" at "+event.getItem().getLocation()+" picked up display item "+is);
				Util.debugLog("Something trying collect QuickShop displayItem, already cancelled. ("+event.getInventory().getLocation().toString()+")");
				event.getItem().remove();
				Util.inventoryCheck(event.getInventory());
			}
		} catch (Exception e) {}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		try {
			ItemStack is = event.getItem();
			if (itemStackCheck(is)) {
				event.setCancelled(true);
				Util.debugLog("Some inventory trying move QuickShop displayItem to another container, already cancelled.");
				event.setItem(new ItemStack(Material.AIR));
				Util.inventoryCheck(event.getDestination());
				Util.inventoryCheck(event.getInitiator());
				Util.inventoryCheck(event.getSource());
			}
		} catch (Exception e) {}
	}

	boolean itemStackCheck(ItemStack is) {
		return DisplayItem.checkShopItem(is);
	}
}
