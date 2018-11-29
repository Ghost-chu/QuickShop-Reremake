package org.maxgamer.quickshop.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.Shop.DisplayItem;
import org.maxgamer.quickshop.Shop.DisplayItemStorage;
import org.maxgamer.quickshop.Util.Util;

import com.bergerkiller.bukkit.common.events.EntityMoveEvent;

public class BKCommonLibListener implements Listener {
	public BKCommonLibListener() {
		Bukkit.getPluginManager().registerEvents(this,Bukkit.getPluginManager().getPlugin("BKCommonLib"));
		
	}
	@EventHandler
	public void onEntityMove(EntityMoveEvent e) {
		if(e.getEntityType()!=EntityType.DROPPED_ITEM) { //Only check we need
			return; 
		}
		Item item = (Item)e.getEntity();
		if(DisplayItem.checkShopItem(item.getItemStack())) {
			if((e.getFromX()==e.getToX())&&(e.getFromZ()==e.getToZ())) //Spam
				return;
			if(e.getEntity().getLocation().getBlock().getType()==Material.WATER) {
				e.getEntity().remove();
				return;
			}
			if(Util.canBeShop(e.getEntity().getLocation().clone().add(0, -1, 0).getBlock())) {
			e.getEntity().teleport(DisplayItemStorage.getDisplayLocation(e.getEntity().getUniqueId()));
			return;
			}else {
				e.getEntity().remove();
				return;
			}
		}
	}
}
