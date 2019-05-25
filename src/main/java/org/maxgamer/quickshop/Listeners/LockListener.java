package org.maxgamer.quickshop.Listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
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
		if (!Util.canBeShop(b,null))
			return;
		Player p = e.getPlayer();
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return; // Didn't right click it, we dont care.
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
		if (!shop.getModerator().isModerator(p.getUniqueId())) {
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
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlace(BlockPlaceEvent e) {
		Block b = e.getBlock();
		try {
			if (b.getType() != Material.HOPPER)
				return;
		} catch (NoSuchFieldError er) {
			return; // Your server doesn't have hoppers
		}
		Player p = e.getPlayer();
		if (!Util.isOtherShopWithinHopperReach(b, p) )
			return;

		if (p.hasPermission("quickshop.other.open")) {
			p.sendMessage(MsgUtil.getMessage("bypassing-lock"));
			return;
		}
		p.sendMessage(MsgUtil.getMessage("that-is-locked"));
		e.setCancelled(true);
	}

	/**
	 * Removes chests when they're destroyed.
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBreak(BlockBreakEvent e) {
		Block b = e.getBlock();
		if(b.getState() instanceof Sign) {
			Sign sign = (Sign)b.getState();
			if(sign.getLine(0).equals(plugin.getConfig().getString("lockette.private"))||sign.getLine(0).equals(plugin.getConfig().getString("lockette.more_users"))){
				//Ignore break lockette sign
				plugin.getLogger().info("Skipped a dead-lock shop sign.(Lockette or other sign-lock plugin)");
				return;
			}
		}
		Player p = e.getPlayer();
		// If the chest was a chest
		if (Util.canBeShop(b,null)) {
			Shop shop = plugin.getShopManager().getShop(b.getLocation());
			if (shop == null)
				return; // Wasn't a shop
			// If they owned it or have bypass perms, they can destroy it
			if (!shop.getOwner().equals(p.getUniqueId()) && !p.hasPermission("quickshop.other.destroy")) {
				e.setCancelled(true);
				p.sendMessage(MsgUtil.getMessage("no-permission"));
				return;
			}
		} else if (Util.isWallSign(b.getType())) {
			if(b instanceof Sign) {
				Sign sign = (Sign)b;
				if(sign.getLine(0).equals(plugin.getConfig().getString("lockette.private"))||sign.getLine(0).equals(plugin.getConfig().getString("lockette.more_users"))){
					//Ignore break lockette sign
					plugin.getLogger().info("Skipped a dead-lock shop sign.(Lockette)");
					return;
				}
			}
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
	@EventHandler(priority = EventPriority.LOW,ignoreCancelled=true)
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