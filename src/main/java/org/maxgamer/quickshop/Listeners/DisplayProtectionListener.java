package org.maxgamer.quickshop.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.DisplayItem;
import org.maxgamer.quickshop.Util.Util;

@SuppressWarnings("deprecation")
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
	@EventHandler
	public void heldItem (PlayerItemHeldEvent e){
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
				plugin.getLogger().warning("[Exploit Alert] A QuickShop item found in "+e.getPlayer().getName()+"'s inventory"+" Deleteing...");
				Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] A QuickShop item found in "+e.getPlayer().getName()+"'s inventory"+" Deleteing...");
				Util.inventoryCheck(e.getPlayer().getInventory());
			}
		} catch (NullPointerException ex) {

		}
	}
	@EventHandler
	public void mendItem (PlayerItemMendEvent e){
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
				plugin.getLogger().warning("[Exploit Alert] A QuickShop item found in "+e.getPlayer().getName()+"'s inventory"+" Deleteing...");
				Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] A QuickShop item found in "+e.getPlayer().getName()+"'s inventory"+" Deleteing...");
				Util.inventoryCheck(e.getPlayer().getInventory());
			}
		} catch (NullPointerException ex) {
		}
	}
	@EventHandler
	public void changeHand (PlayerChangedMainHandEvent e){
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
				plugin.getLogger().warning("[Exploit Alert] A QuickShop item found in "+e.getPlayer().getName()+"'s inventory"+" Deleteing...");
				Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] A QuickShop item found in "+e.getPlayer().getName()+"'s inventory"+" Deleteing...");
				Util.inventoryCheck(e.getPlayer().getInventory());
			}
		} catch (NullPointerException ex) {
		}

	}
	@EventHandler
	public void onPlayerPickup(EntityPickupItemEvent e) {
		ItemStack stack = e.getItem().getItemStack();
		try {
			if (DisplayItem.checkShopItem(stack)) {
				plugin.getLogger().warning("[Exploit Alert] A QuickShop item found in "+e.getEntity().getName()+"'s inventory"+" Deleteing...");
				Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] A QuickShop item found in "+e.getEntity().getName()+"'s inventory"+" Deleteing...");
				e.setCancelled(true);
				// You shouldn't be able to pick up that...
			}
		} catch (NullPointerException ex) {
		} // if meta/displayname/stack is null. We don't really care in that case.
	}
	@EventHandler
	public void onPlayerClick(PlayerInteractEvent e) {
		ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();

		ItemStack stackOffHand = e.getPlayer().getInventory().getItemInOffHand();
		try {
			if (DisplayItem.checkShopItem(stack) || DisplayItem.checkShopItem(stackOffHand)) {
				plugin.getLogger().warning("[Exploit Alert] A QuickShop item found in "+e.getPlayer().getName()+"'s inventory"+" Deleteing...");
				Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] A QuickShop item found in "+e.getPlayer().getName()+"'s inventory"+" Deleteing...");
				e.setCancelled(true);
				Util.inventoryCheck(e.getPlayer().getInventory());
				// You shouldn't be able to pick up that...
			}
		} catch (NullPointerException ex) {
		} // if meta/displayname/stack is null. We don't really care in that case.
	}
	//to support old minecraft version
	@EventHandler
	public void onPlayerPickup_Old(PlayerPickupItemEvent e) {
		ItemStack stack = e.getItem().getItemStack();
		try {
			if (DisplayItem.checkShopItem(stack)) {
				plugin.getLogger().warning("[Exploit Alert] A QuickShop item found in "+e.getPlayer().getName()+"'s inventory"+" Deleteing...");
				Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] A QuickShop item found in "+e.getPlayer().getName()+"'s inventory"+" Deleteing...");
				e.setCancelled(true);
				Util.inventoryCheck(e.getPlayer().getInventory());
				// You shouldn't be able to pick up that...
			}
		} catch (NullPointerException ex) {
		} // if meta/displayname/stack is null. We don't really care in that case.
	}
	@EventHandler
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
				plugin.getLogger().warning("[Exploit Alert] A QuickShop item found in "+e.getPlayer().getName()+"'s inventory"+" Deleteing...");
				Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] A QuickShop item found in "+e.getPlayer().getName()+"'s inventory"+" Deleteing...");
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
	
	@EventHandler(ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		try {
			ItemStack is = event.getItem();
			if (itemStackCheck(is)) {
				event.setCancelled(true);
				plugin.getLogger().warning("[Exploit alert] Inventory "+event.getDestination().getTitle()+" move display item "+is);
				Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] Inventory "+event.getDestination().getTitle()+" move display item "+is);
				event.setItem(new ItemStack(Material.AIR));
			}
		} catch (Exception e) {}
	}

	boolean itemStackCheck(ItemStack is) {
		return DisplayItem.checkShopItem(is);
	}
}
