package org.maxgamer.quickshop.Shop;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShopPurchaseEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	private Shop shop;
	private Player p;
	private int amount;
	private boolean cancelled;

	/**
	 * Builds a new shop purchase event
	 * 
	 * @param shop
	 *            The shop bought from
	 * @param p
	 *            The player buying
	 * @param amount
	 *            The amount they're buying
	 */
	public ShopPurchaseEvent(Shop shop, Player p, int amount) {
		this.shop = shop;
		this.p = p;
		this.amount = amount;
	}

	/**
	 * The shop used in this event
	 * 
	 * @return The shop used in this event
	 */
	public Shop getShop() {
		return this.shop;
	}

	/**
	 * The player trading with the shop
	 * 
	 * @return The player trading with the shop
	 */
	public Player getPlayer() {
		return this.p;
	}

	/**
	 * The amount the purchase was for
	 * 
	 * @return The amount the purchase was for
	 */
	public int getAmount() {
		return this.amount;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return this.cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}
}