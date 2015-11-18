package org.maxgamer.quickshop.Listeners;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Info;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopAction;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

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
	 * Handles players left clicking a chest. Left click a NORMAL chest with
	 * item : Send creation menu Left click a SHOP chest : Send purchase menu
	 */
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.MONITOR)
	public void onClick(PlayerInteractEvent e) {
		if (e.getAction() != Action.LEFT_CLICK_BLOCK)
			return;
		Block b = e.getClickedBlock();
		if (!Util.canBeShop(b) && b.getType() != Material.WALL_SIGN)
			return;
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
			} else if (b.getType() == Material.CHEST || b.getType() == Material.TRAPPED_CHEST) {
				attached = Util.getSecondHalf(b);
			}
			
			if (attached != null) {
				shop = plugin.getShopManager().getShop(attached.getLocation());
			}
		}
		// Purchase handling
		if (shop != null && p.hasPermission("quickshop.use") && (plugin.sneakTrade == false || p.isSneaking())) {
			shop.onClick();
			// Text menu
			MsgUtil.sendShopInfo(p, shop);
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
		else if (!e.isCancelled() && shop == null && item != null && item.getType() != Material.AIR && p.hasPermission("quickshop.create.sell") && Util.canBeShop(b) && p.getGameMode() != GameMode.CREATIVE && (plugin.sneakCreate == false || p.isSneaking())) {
			if (!plugin.getShopManager().canBuildShop(p, b, e.getBlockFace())) {
				// As of the new checking system, most plugins will tell the
				// player why they can't create a shop there.
				// So telling them a message would cause spam etc.
				return;
			}
			if (Util.getSecondHalf(b) != null && !p.hasPermission("quickshop.create.double")) {
				p.sendMessage(MsgUtil.getMessage("no-double-chests"));
				return;
			}
			if (Util.isBlacklisted(item.getType()) && !p.hasPermission("quickshop.bypass." + item.getTypeId())) {
				p.sendMessage(MsgUtil.getMessage("blacklisted-item"));
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
			p.sendMessage(MsgUtil.getMessage("how-much-to-trade-for", Util.getName(info.getItem())));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
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

	@EventHandler
	public void onTeleport(PlayerTeleportEvent e) {
		PlayerMoveEvent me = new PlayerMoveEvent(e.getPlayer(), e.getFrom(), e.getTo());
		onMove(me);
	}

	@EventHandler
	public void onJoin(final PlayerJoinEvent e) {
		// Notify the player any messages they were sent
		Bukkit.getScheduler().runTaskLater(QuickShop.instance, new Runnable() {
			@Override
			public void run() {
				MsgUtil.flush(e.getPlayer());
			}
		}, 60);
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		// Remove them from the menu
		plugin.getShopManager().getActions().remove(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onPlayerPickup(PlayerPickupItemEvent e) {
		ItemStack stack = e.getItem().getItemStack();
		try {
			if (stack.getItemMeta().getDisplayName().startsWith(ChatColor.RED + "QuickShop ")) {
				e.setCancelled(true);
				// You shouldn't be able to pick up that...
			}
		} catch (NullPointerException ex) {
		} // if meta/displayname/stack is null. We don't really care in that
			// case.
	}
}