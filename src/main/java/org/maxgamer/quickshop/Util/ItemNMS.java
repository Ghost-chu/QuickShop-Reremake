package org.maxgamer.quickshop.Util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.maxgamer.quickshop.QuickShop;

public class ItemNMS {	
	public String getItemJSON(ItemStack iStack) {
		ItemStack itemStack = iStack.clone();
		String name = Bukkit.getServer().getClass().getPackage().getName();
		String version = name.substring(name.lastIndexOf('.') + 1);
		Class<?> nmsClass;
		try {
			nmsClass = Class.forName("org.maxgamer.quickshop.Nms." + version);
		} catch (ClassNotFoundException e2) {
			return null;
		}
		Object obj;
		try {
			obj = nmsClass.newInstance();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			return null;
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			return null;
		}
		try {
			Method method = nmsClass.getMethod("getNBTJson", ItemStack.class);
			String itemJSON = (String) method.invoke(obj, new Object[] { itemStack });
			return itemJSON;
		} catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException
				| IllegalAccessException e) {
			// do somethings
			QuickShop.instance.getLogger().info("A error happend:");
			e.printStackTrace();
			return null;
		}
	}
	

}
