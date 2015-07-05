package org.maxgamer.quickshop.Listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

public class LockListener implements Listener {
	private QuickShop plugin;

	public LockListener(QuickShop plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onClick(PlayerInteractEvent e) {
		Block b = e.getClickedBlock();
		Player p = e.getPlayer();
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return; // Didn't right click it, we dont care.
		if (!Util.canBeShop(b))
			return; // Interacted with air
		Shop shop = plugin.getShopManager().getShop(b.getLocation());
		// Make sure they're not using the non-shop half of a double chest.
		if (shop == null) {
			b = Util.getSecondHalf(b);
			if (b == null)
				return;
			shop = plugin.getShopManager().getShop(b.getLocation());
			if (shop == null)
				return;
		}
		if (!shop.getOwner().equals(p.getUniqueId())) {
			if (p.hasPermission("quickshop.other.open")) {
				p.sendMessage(MsgUtil.getMessage("bypassing-lock"));
				return;
			}
			p.sendMessage(MsgUtil.getMessage("that-is-locked"));
			e.setCancelled(true);
			return;
		}
	}

	/**
	 * Handles hopper placement
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onPlace(BlockPlaceEvent e) {
		Block b = e.getBlock();
		try {
			if (b.getType() != Material.HOPPER)
				return;
		} catch (NoSuchFieldError er) {
			return; // Your server doesn't have hoppers
		}
		Block c = e.getBlockAgainst();
		if (Util.canBeShop(c) == false)
			return;
		Player p = e.getPlayer();
		Shop shop = plugin.getShopManager().getShop(c.getLocation());
		if (shop == null) {
			c = Util.getSecondHalf(c);
			if (c == null)
				return; // You didn't place a hopper on a shop. Meh.
			else
				shop = plugin.getShopManager().getShop(c.getLocation());
			if (shop == null)
				return;
		}
		if (p.getUniqueId().equals(shop.getOwner()) == false) {
			if (p.hasPermission("quickshop.other.open")) {
				p.sendMessage(MsgUtil.getMessage("bypassing-lock"));
				return;
			}
			p.sendMessage(MsgUtil.getMessage("that-is-locked"));
			e.setCancelled(true);
			return;
		}
	}

	/**
	 * Removes chests when they're destroyed.
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBreak(BlockBreakEvent e) {
		Block b = e.getBlock();
		Player p = e.getPlayer();
		// If the chest was a chest
		if (Util.canBeShop(b)) {
			Shop shop = plugin.getShopManager().getShop(b.getLocation());
			if (shop == null)
				return; // Wasn't a shop
			// If they owned it or have bypass perms, they can destroy it
			if (!shop.getOwner().equals(p.getUniqueId()) && !p.hasPermission("quickshop.other.destroy")) {
				e.setCancelled(true);
				p.sendMessage(MsgUtil.getMessage("no-permission"));
				return;
			}
		} else if (b.getType() == Material.WALL_SIGN) {
			b = Util.getAttached(b);
			if (b == null)
				return;
			Shop shop = plugin.getShopManager().getShop(b.getLocation());
			if (shop == null)
				return;
			// If they're the shop owner or have bypass perms, they can destroy
			// it.
			if (!shop.getOwner().equals(p.getUniqueId()) && !p.hasPermission("quickshop.other.destroy")) {
				e.setCancelled(true);
				p.sendMessage(MsgUtil.getMessage("no-permission"));
				return;
			}
		}
	}

	/**
	 * Handles shops breaking through explosions
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onExplode(EntityExplodeEvent e) {
		if (e.isCancelled())
			return;
		for (int i = 0; i < e.blockList().size(); i++) {
			Block b = e.blockList().get(i);
			Shop shop = plugin.getShopManager().getShop(b.getLocation());
			if (shop != null) {
				// ToDo: Shouldn't I be decrementing 1 here? Concurrency and
				// all..
				e.blockList().remove(b);
			}
		}
	}
}