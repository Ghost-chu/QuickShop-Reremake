package org.maxgamer.quickshop.Listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Command.QS;
import org.maxgamer.quickshop.Shop.Info;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopAction;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

import java.util.HashMap;
import java.util.UUID;

public class PlayerListener implements Listener {
	private QuickShop plugin;

	public PlayerListener(QuickShop plugin) {
		this.plugin = plugin;
	}

	/*
	 * Could be useful one day private LinkedList<String> getParents(Class<?>
	 * clazz){ LinkedList<String> classes = new LinkedList<String>();
	 * 
	 * while(clazz != null){ classes.add("Extends " + ChatColor.GREEN +
	 * clazz.getCanonicalName()); for(Class<?> iface : clazz.getInterfaces()){
	 * classes.add("Implements " + ChatColor.RED + iface.getCanonicalName());
	 * classes.addAll(getParents(iface)); }
	 * 
	 * clazz = clazz.getSuperclass(); } return classes; }
	 */
	/**
	 * Handles players left clicking a chest. Left click a NORMAL chest with item :
	 * Send creation menu Left click a SHOP chest : Send purchase menu
	 */
	@EventHandler(priority = EventPriority.MONITOR,ignoreCancelled=true)
	public void onClick(PlayerInteractEvent e) {
		if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
			Block b = e.getClickedBlock();
			if (!Util.canBeShop(b,null,true) && b.getType() != Material.WALL_SIGN) {
				Util.debugLog("No shop block");
				return;
			}
			Player p = e.getPlayer();
			if (plugin.sneak && !p.isSneaking()) {
				// Sneak only
				return;
			}
			Location loc = b.getLocation();
			ItemStack item = e.getItem();
			// Get the shop
			Shop shop = plugin.getShopManager().getShop(loc);
			// If that wasn't a shop, search nearby shops
			if (shop == null) {
				Block attached = null;

				if (b.getType() == Material.WALL_SIGN) {
					attached = Util.getAttached(b);
					if (attached != null) {
						shop = plugin.getShopManager().getShop(attached.getLocation());
					}
				} else if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
					attached = Util.getSecondHalf(b);
					if (attached != null) {
						Shop secondHalfShop = plugin.getShopManager().getShop(attached.getLocation());
						if (secondHalfShop != null && !p.getUniqueId().equals(secondHalfShop.getOwner())) {
							// If player not the owner of the shop, make him select the second half of the
							// shop
							// Otherwise owner will be able to create new double chest shop
							shop = secondHalfShop;
						}
					}
				}
			}
			// Purchase handling
			if (shop != null && p.hasPermission("quickshop.use") && (plugin.sneakTrade == false || p.isSneaking())) {
				shop.onClick();
				// Text menu
				MsgUtil.sendShopInfo(p, shop);
				shop.setSignText();
				if (shop.isSelling()) {
					p.sendMessage(MsgUtil.getMessage("how-many-buy"));
				} else {
					int items = Util.countItems(p.getInventory(), shop.getItem());
					p.sendMessage(MsgUtil.getMessage("how-many-sell", "" + items));
				}
				// Add the new action
				HashMap<UUID, Info> actions = plugin.getShopManager().getActions();
				Info info = new Info(shop.getLocation(), ShopAction.BUY, null, null, shop);
				actions.put(p.getUniqueId(), info);
				return;
			}
			// Handles creating shops
			else if (!e.isCancelled() && shop == null && item != null && item.getType() != Material.AIR
					&& p.hasPermission("quickshop.create.sell") && Util.canBeShop(b,null,true)
					&& p.getGameMode() != GameMode.CREATIVE) {
				if(e.isCancelled())
					return;
				if(plugin.getConfig().getBoolean("shop.sneak-to-control")&&!p.isSneaking())
					return;
				if (!plugin.getShopManager().canBuildShop(p, b, e.getBlockFace())) {
					// As of the new checking system, most plugins will tell the
					// player why they can't create a shop there.
					// So telling them a message would cause spam etc.
					Util.debugLog("Can't be shop");
					return;
				}
				if (Util.getSecondHalf(b) != null && !p.hasPermission("quickshop.create.double")) {
					p.sendMessage(MsgUtil.getMessage("no-double-chests"));
					return;
				}
				if (Util.isBlacklisted(item.getType())
						&& !p.hasPermission("quickshop.bypass." + item.getType().name())) {
					p.sendMessage(MsgUtil.getMessage("blacklisted-item"));
					return;
				}
				if (b.getType()==Material.ENDER_CHEST) {
					if(!p.hasPermission("quickshop.create.enderchest")) {
						Util.debugLog("No permission");
						Util.debugLog(""+p.hasPermission("quickshop.create.enderchest"));
						return;
					}
				}
				if (!Util.canBeShop(b,e.getPlayer().getUniqueId(),false) && b.getType() != Material.WALL_SIGN) {
					Util.debugLog("Can't create shop there");
					return;
				}
				// Finds out where the sign should be placed for the shop
				Block last = null;
				Location from = p.getLocation().clone();
				from.setY(b.getY());
				from.setPitch(0);
				BlockIterator bIt = new BlockIterator(from, 0, 7);
				while (bIt.hasNext()) {
					Block n = bIt.next();
					if (n.equals(b))
						break;
					last = n;
				}
				// Send creation menu.
				Info info = new Info(b.getLocation(), ShopAction.CREATE, e.getItem(), last);
				plugin.getShopManager().getActions().put(p.getUniqueId(), info);
				p.sendMessage(
						MsgUtil.getMessage("how-much-to-trade-for", MsgUtil.getItemi18n(Util.getName(info.getItem()))));
			}
		} else if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)
				&& e.getClickedBlock().getType() == Material.WALL_SIGN) {
			Block block;
			if (e.getClickedBlock().getType() == Material.WALL_SIGN) {
				block = Util.getAttached(e.getClickedBlock());
			} else {
				block = e.getClickedBlock();
			}
			if (plugin.getShopManager().getShop(block.getLocation()) != null && plugin.getShopManager().getShop(block.getLocation()).getOwner().equals(e.getPlayer().getUniqueId())) {
				MsgUtil.sendControlPanelInfo((CommandSender) e.getPlayer(),
						plugin.getShopManager().getShop(block.getLocation()));
				plugin.getShopManager().getShop(block.getLocation()).setSignText();
			}
		}

	}

	@EventHandler(priority = EventPriority.HIGH,ignoreCancelled=true)
	/**
	 * Waits for a player to move too far from a shop, then cancels the menu.
	 */
	public void onMove(PlayerMoveEvent e) {
		if (e.isCancelled())
			return;
		Info info = plugin.getShopManager().getActions().get(e.getPlayer().getUniqueId());
		if (info != null) {
			Player p = e.getPlayer();
			Location loc1 = info.getLocation();
			Location loc2 = p.getLocation();
			if (loc1.getWorld() != loc2.getWorld() || loc1.distanceSquared(loc2) > 25) {
				if (info.getAction() == ShopAction.CREATE) {
					p.sendMessage(MsgUtil.getMessage("shop-creation-cancelled"));
				} else if (info.getAction() == ShopAction.BUY) {
					p.sendMessage(MsgUtil.getMessage("shop-purchase-cancelled"));
				}
				plugin.getShopManager().getActions().remove(p.getUniqueId());
				return;
			}
		}
	}

	@EventHandler(ignoreCancelled=true)
	public void onTeleport(PlayerTeleportEvent e) {
		PlayerMoveEvent me = new PlayerMoveEvent(e.getPlayer(), e.getFrom(), e.getTo());
		onMove(me);
		Util.inventoryCheck(e.getPlayer().getInventory());
	}

	@EventHandler(ignoreCancelled=true)
	public void onJoin(PlayerJoinEvent e) {
		// Notify the player any messages they were sent
		Bukkit.getScheduler().runTaskLater(QuickShop.instance, new Runnable() {
			@Override
			public void run() {
				MsgUtil.flush(e.getPlayer());
				Util.inventoryCheck(e.getPlayer().getInventory());
			}
		}, 60);
	}

	@EventHandler(ignoreCancelled=true)
	public void onPlayerQuit(PlayerQuitEvent e) {
		// Remove them from the menu
		plugin.getShopManager().getActions().remove(e.getPlayer().getUniqueId());
		QS.signPlayerCache.remove(e.getPlayer().getUniqueId());
	}
	@EventHandler(priority=EventPriority.MONITOR,ignoreCancelled=true)
	public void onInventoryClose(InventoryCloseEvent e) {
		Util.debugLog("Inventory closed.");
		try {
			Inventory inventory = e.getInventory();
			if (inventory == null) {
				Util.debugLog("Inventory: null");
				return;
			}
			Util.debugLog("Inventory: "+inventory.toString());
			Location location = inventory.getLocation();
			if (location == null) {
				Util.debugLog("Location: null");
				return;
			}
			Util.debugLog("Location: "+location.toString());
			Shop shop = plugin.getShopManager().getShop(location);
			if (shop == null) {
				Util.debugLog("Shop: null");
				return;
			}
			Util.debugLog("Shop: "+shop.toString());
			Util.debugLog("Updateing shops..");
			shop.setSignText();
		} catch (Throwable t) {
			
		}
	}

}
