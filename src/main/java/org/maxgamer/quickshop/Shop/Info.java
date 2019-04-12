package org.maxgamer.quickshop.Shop;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class Info {
	private Location loc;
	private ShopAction action;
	private ItemStack item;
	private Block last;
	private Shop shop;

	public Info(Location loc, ShopAction action, ItemStack item, Block last) {
		this.loc = loc;
		this.action = action;
		this.last = last;
		if (item != null)
			this.item = item.clone();
	}

	public Info(Location loc, ShopAction action, ItemStack item, Block last, Shop shop) {
		this.loc = loc;
		this.action = action;
		this.last = last;
		if (item != null)
			this.item = item.clone();
		if (shop != null) {
			this.shop = shop.clone();
		}
	}
	/** 
	 * Get shop is or not has changed.
	 * @param Shop shop,  The need checked with this shop.
	 * */
	public boolean hasChanged(Shop shop) {
		if (this.shop.isUnlimited() != shop.isUnlimited())
			return true;
		if (this.shop.getShopType() != shop.getShopType())
			return true;
		if (!this.shop.getOwner().equals(shop.getOwner()))
			return true;
		if (this.shop.getPrice() != shop.getPrice())
			return true;
		if (!this.shop.getLocation().equals(shop.getLocation()))
			return true;
		if (!this.shop.matches(shop.getItem()))
			return true;
		return false;
	}
	/**
	 * @return ShopAction action, Get shop action.
	 * */
	public ShopAction getAction() {
		return this.action;
	}
	/**
	 * @return Location loc, Get shop's location,
	 * */
	public Location getLocation() {
		return this.loc;
	}

	/*
	 * public Material getMaterial(){ return this.item.getType(); } public byte
	 * getData(){ return this.getData(); }
	 */
	/**
	 * @return ItemStack iStack, Get Shop's selling/buying item's ItemStack.
	 * */
	public ItemStack getItem() {
		return this.item;
	}

	public void setAction(ShopAction action) {
		this.action = action;
	}
	/**
	 * @return Block signBlock, Get block of shop's sign, may return the null.
	 * */
	public Block getSignBlock() {
		return this.last;
	}
}