package org.maxgamer.quickshop.Shop;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.maxgamer.quickshop.QuickShop;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
//import org.maxgamer.quickshop.Util.NMS;


/**
 * @author Netherfoam A display item, that spawns a block above the chest and
 *         cannot be interacted with.
 */
public class DisplayItem {
	private Shop shop;
	private ItemStack iStack;
	private Item item;
	static QuickShop plugin = QuickShop.instance;
	private java.util.List<?> itemlist;
	private java.util.List<?> lorelist;
	private java.util.List<?> displaynamelist;

	// private Location displayLoc;
	/**ZZ
	 * Creates a new display item.
	 * 
	 * @param shop
	 *            The shop (See Shop)
	 * @param iStack
	 *            The item stack to clone properties of the display item from.
	 */
	public DisplayItem(Shop shop, ItemStack iStack) {
		this.shop = shop;
		this.iStack = iStack.clone();
		
		// this.displayLoc = shop.getLocation().clone().add(0.5, 1.2, 0.5);
	}

	/**
	 * Spawns the dummy item on top of the shop.
	 */
	public void spawn() {
		if (shop.getLocation().getWorld() == null)
			return;
		Location dispLoc = this.getDisplayLocation();
		// Check is or not in blacklist/whitelist
		boolean showFloatItem = true;
		if (plugin.getConfig().getBoolean("float.enable")) {
			// Enabled! Check start!
			// Item
			boolean found_item = false;
			boolean found_lore = false;
			boolean found_displayname = false;
			if (plugin.getConfig().getBoolean("float.item.enable")) {
				boolean blacklist = plugin.getConfig().getBoolean("float.item.blacklist");
				itemlist = plugin.getConfig().getList("float.item.list");
				for (Object material : itemlist) {
					String materialname = String.valueOf(material);
//					String itemname = iStack.getType().name();
					if (Material.getMaterial(materialname).equals(iStack.getType())) {
						found_item = true;
						break;
					} else {
						plugin.getLogger().info(materialname + " not a bukkit item.");
					}
				}
				if (blacklist) {
					if (found_item) {
						return;
					}
				} else {
					if (!found_item) {
						return;
					}
				}
			}
			if (!showFloatItem) {
				return;
			}
			// End Item check

			// DisplayName
			if (plugin.getConfig().getBoolean("float.displayname.enable")) {
				boolean blacklist = plugin.getConfig().getBoolean("float.displayname.blacklist");
				displaynamelist = plugin.getConfig().getList("float.displayname.list");
				if (!iStack.hasItemMeta()) {
					found_displayname = false;
				} else {
					String itemname = iStack.getItemMeta().getDisplayName();
					for (Object name : displaynamelist) {
						String listname = String.valueOf(name);
						if (itemname.contains(listname)) {
							found_displayname = true;
							break;
						}
					}
				}
				if (blacklist) {
					if (found_displayname) {
						showFloatItem = false;
					}
				} else {
					if (!found_displayname) {
						showFloatItem = false;
					}
				}
				if (!showFloatItem) {
					return;
				}
				// End DisplayName check
			}

			// Lore
			if (plugin.getConfig().getBoolean("float.lore.enable")) {
				boolean blacklist = plugin.getConfig().getBoolean("float.lore.blacklist");
				lorelist = plugin.getConfig().getList("float.lore.list");
				if (!iStack.hasItemMeta()) {
					found_lore = false;
				} else {
					java.util.List<String> itemlores = iStack.getItemMeta().getLore();
					for (String loreinItem : itemlores) {
						String loreinItem_String = loreinItem;
						for (Object loreinList : lorelist) {
							String loreinList_String = String.valueOf(loreinList);
							if (loreinItem_String.contains(loreinList_String)) {
								found_lore = true;
								break;
							}
						}
						if (found_lore) {
							break;
						}
					}
					if (blacklist) {
						if (found_lore) {
							showFloatItem = false;
						}
					} else {
						if (!found_lore) {
							showFloatItem = false;
						}
					}
					if (!showFloatItem) {
						return;
					}

				}
			}
		}
		// Check end
		if (!showFloatItem) {
			return;
		}
		//Call Event for QSAPI
		
			ShopDisplayItemSpawnEvent shopDisplayItemSpawnEvent = new ShopDisplayItemSpawnEvent(shop, iStack);
			Bukkit.getPluginManager().callEvent(shopDisplayItemSpawnEvent);
			ShopDisplayItemSpawnEvent shopDisplayItemSpawnEvent_v2 = new ShopDisplayItemSpawnEvent(shop, iStack, false);
			Bukkit.getPluginManager().callEvent(shopDisplayItemSpawnEvent_v2);
			if (shopDisplayItemSpawnEvent.isCancelled()) {
				return;
			}
			this.item = shop.getLocation().getWorld().dropItem(dispLoc, this.iStack);
			this.item.setVelocity(new Vector(0, 0.1, 0));
			try {
				this.safeGuard(this.item);
				// NMS.safeGuard
			} catch (Exception e) {
				e.printStackTrace();
				plugin.getLogger().log(Level.WARNING,
						"QuickShop version mismatch! This version of QuickShop is incompatible with this version of bukkit! Try update?");
			}
	}

	/**
	 * Spawns the new display item. Does not remove duplicate items.
	 */
	public void respawn() {
		remove();
		spawn();
	}
	public void safeGuard(Item item) {
		item.setPickupDelay(Integer.MAX_VALUE);
		ItemMeta iMeta = item.getItemStack().getItemMeta();
		
		if(plugin.getConfig().getBoolean("shop.display-item-use-name")) {
			item.setCustomName("QuickShop");
			iMeta.setDisplayName("QuickShop");
		}
		item.setPortalCooldown(Integer.MAX_VALUE);
		item.setSilent(true);
		item.setInvulnerable(true);
		java.util.List<String> lore = new ArrayList<String>();
	    lore.add("QuickShop DisplayItem");
		iMeta.setLore(lore);
		item.getItemStack().setItemMeta(iMeta);
	}
	public static boolean checkShopItem(ItemStack itemStack){
		if(!itemStack.hasItemMeta()){
			return false;
		}
		if(itemStack.getItemMeta().hasDisplayName() && itemStack.getItemMeta().getDisplayName().contains("QuickShop")){
			return true;
		}
		if(itemStack.getItemMeta().hasLore()) {
			List<String> lores = itemStack.getItemMeta().getLore();
			for (String singleLore : lores) {
				if (singleLore.equals("QuickShop DisplayItem")) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Removes all items floating ontop of the chest that aren't the display
	 * item.
	 */
	public boolean removeDupe() {
		if (shop.getLocation().getWorld() == null)
			return false;
		Location displayLoc = shop.getLocation().getBlock().getRelative(0, 1, 0).getLocation();
		boolean removed = false;
		Chunk c = displayLoc.getChunk();
		for (Entity e : c.getEntities()) {
			if (!(e instanceof Item))
				continue;
			if (this.item != null && e.getEntityId() == this.item.getEntityId())
				continue;
			Location eLoc = e.getLocation().getBlock().getLocation();
			if (eLoc.equals(displayLoc) || eLoc.equals(shop.getLocation())) {
				ItemStack near = ((Item) e).getItemStack();
				// if its the same its a dupe
				if (this.shop.matches(near)) {
					e.remove();
					removed = true;
				}
			}
		}
		return removed;
		
	}

	/**
	 * Removes the display item.
	 */
	public void remove() {
		if (this.item == null)
			return;
		this.item.remove();
		this.item = null;
	}

	/**
	 * @return Returns the exact location of the display item. (1 above shop
	 *         block, in the center)
	 */
	public Location getDisplayLocation() {
		return this.shop.getLocation().clone().add(0.5, 1.2, 0.5);
	}

	/**
	 * Returns the reference to this shops item. Do not modify.
	 */
	public Item getItem() {
		return this.item;
	}
}