package org.maxgamer.quickshop.Listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.InventoryHolder;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Info;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopAction;
import org.maxgamer.quickshop.Util.MsgUtil;
import org.maxgamer.quickshop.Util.Util;

import net.md_5.bungee.api.ChatColor;

public class BlockListener implements Listener {
	private QuickShop plugin;

	public BlockListener(QuickShop plugin) {
		this.plugin = plugin;
	}

	/**
	 * Listens for chest placement, so a doublechest shop can't be created.
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlace(BlockPlaceEvent e) {
		if (e.isCancelled())
			return;
		BlockState bs = e.getBlock().getState();
		if (bs instanceof DoubleChest == false)
			return;
		Block b = e.getBlock();
		Player p = e.getPlayer();
		Block chest = Util.getSecondHalf(b);
		if (chest != null && plugin.getShopManager().getShop(chest.getLocation()) != null && !p.hasPermission("quickshop.create.double")) {
			e.setCancelled(true);
			p.sendMessage(MsgUtil.getMessage("no-double-chests"));
		}
	}

	/**
	 * Removes chests when they're destroyed.
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBreak(BlockBreakEvent e) {
		Block b = e.getBlock();
		Player p = e.getPlayer();
		// If the shop was a chest
		if (b.getState() instanceof InventoryHolder) {
			Shop shop = plugin.getShopManager().getShop(b.getLocation());
			if (shop == null)
				return;
			// If they're either survival or the owner, they can break it
			if (p.getGameMode() == GameMode.CREATIVE && !p.getUniqueId().equals(shop.getOwner())) {
				e.setCancelled(true);
				p.sendMessage(MsgUtil.getMessage("no-creative-break"));
				return;
			}
			// Cancel their current menu... Doesnt cancel other's menu's.
			Info action = plugin.getShopManager().getActions().get(p.getName());
			if (action != null) {
				action.setAction(ShopAction.CANCELLED);
			}
			shop.delete();
			p.sendMessage(MsgUtil.getMessage("success-removed-shop"));
		} else if (b.getType() == Material.WALL_SIGN) {
			Shop shop = getShopNextTo(b.getLocation());
			if (shop == null)
				return;
			// If they're in creative and not the owner, don't let them
			// (accidents happen)
			if (p.getGameMode() == GameMode.CREATIVE && !p.getUniqueId().equals(shop.getOwner())) {
				e.setCancelled(true);
				p.sendMessage(MsgUtil.getMessage("no-creative-break"));
				return;
			}
			if (e.isCancelled())
				return;
			e.setCancelled(true); // Cancel the event so that the sign does not
			// drop.. TODO: Find a better way.
			b.setType(Material.AIR);
		}
	}

	/**
	 * Handles shops breaking through explosions
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onExplode(EntityExplodeEvent e) {
		if (e.isCancelled())
			return;
		for (int i = 0; i < e.blockList().size(); i++) {
			Block b = e.blockList().get(i);
			Shop shop = plugin.getShopManager().getShop(b.getLocation());
			if (shop != null) {
				shop.delete();
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled=true)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		if (!plugin.display) {
			return;
		}
		
		Block block = event.getBlock().getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
		Shop shop = plugin.getShopManager().getShop(block.getLocation());
		if (shop != null) {
			event.setCancelled(true);
			plugin.getLogger().warning("[Exploit Alert] a piston tried to move the item on top of "+shop);
			Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] A piston tried to move the item on top of "+shop);
			return;
		}
		
		for (Block oBlock : event.getBlocks()) {
			Block otherBlock = oBlock.getRelative(event.getDirection()).getRelative(BlockFace.DOWN);
			if (Util.canBeShop(otherBlock)) {
				shop = plugin.getShopManager().getShop(otherBlock.getLocation());
				if (shop!=null) {
					event.setCancelled(true);
					plugin.getLogger().warning("[Exploit Alert] a piston tried to move the item on top of "+shop);
					Util.sendMessageToOps(ChatColor.RED+"[QuickShop][Exploit alert] A piston tried to move the item on top of "+shop);
					return;
				}
			}
		}
	}

	/**
	 * Gets the shop a sign is attached to
	 * 
	 * @param loc
	 *            The location of the sign
	 * @return The shop
	 */
	private Shop getShopNextTo(Location loc) {
		Block b = Util.getAttached(loc.getBlock());
		// Util.getAttached(b)
		if (b == null)
			return null;
		return plugin.getShopManager().getShop(b.getLocation());
	}
}