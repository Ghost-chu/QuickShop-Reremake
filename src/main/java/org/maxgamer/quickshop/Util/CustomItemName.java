package org.maxgamer.quickshop.Util;

import org.bukkit.inventory.ItemStack;

public class CustomItemName {
	private ItemStack itemStack;
	private String signName, fullName;
	
	public CustomItemName(ItemStack itemStack, String signName, String fullName) {
		this.itemStack = itemStack;
		this.signName = signName;
		this.fullName = fullName;
	}
	
	public ItemStack getItemStack() {
		return itemStack;
	}
	public String getSignName() {
		return signName;
	}
	public String getFullName() {
		return fullName;
	}
	public boolean matches(ItemStack otherItemStack) {
		return otherItemStack.getType()==this.itemStack.getType() && otherItemStack.getDurability()==this.itemStack.getDurability();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
		result = prime * result + ((itemStack == null) ? 0 : itemStack.getType().hashCode());
		result = prime * result + ((signName == null) ? 0 : signName.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CustomItemName)) {
			return false;
		}
		CustomItemName other = (CustomItemName) obj;
		if (fullName == null) {
			if (other.fullName != null) {
				return false;
			}
		} else if (!fullName.equals(other.fullName)) {
			return false;
		}
		if (itemStack.getItemMeta() == null) {
			if (other.itemStack != null) {
				return false;
			}
		} else if (!itemStack.isSimilar(other.itemStack)) {
			return false;
		}
		if (signName == null) {
			if (other.signName != null) {
				return false;
			}
		} else if (!signName.equals(other.signName)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "CustomItemName [itemStack=" + itemStack + ", signName=" + signName + ", fullName=" + fullName + "]";
	}
	
	
}
