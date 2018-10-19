package org.maxgamer.quickshop.Nms;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import net.minecraft.server.v1_13_R1.NBTTagCompound;

public class v1_13_R1 {
	public String getNBTJson(ItemStack iStack) {
		if(iStack.getType().equals(Material.AIR)) {
			return null;
		}
		net.minecraft.server.v1_13_R1.ItemStack mcitem = CraftItemStack.asNMSCopy(iStack);
		NBTTagCompound nbt = new NBTTagCompound();
		mcitem.save(nbt);
		return nbt.toString();
	}
}
