package org.maxgamer.quickshop.Shop;

import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract interface Shop {
	public abstract Shop clone();

	public abstract String getRemainingStock();

	public abstract String getRemainingSpace();

	public abstract boolean matches(ItemStack paramItemStack);

	public abstract Location getLocation();

	public abstract double getPrice();

	public abstract void setPrice(double paramDouble);

	public abstract void update();

	public abstract short getDurability();

	public abstract UUID getOwner();

	public abstract ItemStack getItem();

	public abstract void remove(ItemStack paramItemStack, int paramInt);

	public abstract void add(ItemStack paramItemStack, int paramInt);

	public abstract void sell(Player paramPlayer, int paramInt);

	public abstract void buy(Player paramPlayer, int paramInt);

	public abstract void setOwner(UUID paramString);

	public abstract void setUnlimited(boolean paramBoolean);

	public abstract boolean isUnlimited();

	public abstract ShopType getShopType();

	public abstract boolean isBuying();

	public abstract boolean isSelling();

	public abstract void setShopType(ShopType paramShopType);

	public abstract void setSignText();

	public abstract void setSignText(String[] paramArrayOfString);

	public abstract List<Sign> getSigns();

	public abstract boolean isAttached(Block paramBlock);

	public abstract String getDataName();

	public abstract void delete();

	public abstract void delete(boolean paramBoolean);

	public abstract boolean isValid();

	public abstract void onUnload();

	public abstract void onLoad();

	public abstract void onClick();
	
	public abstract String ownerName();
}