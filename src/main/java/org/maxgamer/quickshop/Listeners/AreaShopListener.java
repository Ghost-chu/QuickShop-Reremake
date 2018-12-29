package org.maxgamer.quickshop.Listeners;

import java.util.Iterator;
import org.maxgamer.quickshop.Util.*;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import me.wiefferink.areashop.events.notify.UnrentedRegionEvent;

public class AreaShopListener implements Listener {
	QuickShop plugin = QuickShop.instance;
	
	@EventHandler
	public void unRentedArea(UnrentedRegionEvent e) {
		Vector areaMaxVector = e.getRegion().getMaximumPoint();
		Vector areaMinVector = e.getRegion().getMinimumPoint();
		int minX = areaMinVector.getBlockX();
		int maxX = areaMaxVector.getBlockX();
		int minY = areaMinVector.getBlockY();
		int maxY = areaMaxVector.getBlockY();
		int minZ = areaMinVector.getBlockZ();
		int maxZ = areaMaxVector.getBlockZ();
		Iterator<Shop> shops = plugin.getShopManager().getShopIterator();
		while (shops.hasNext()) {
			Shop shop = shops.next();
			if(shop.getLocation().getWorld().getName()==e.getRegion().getWorld().getName()) {
				int bX = shop.getLocation().getBlockX();
				int bY = shop.getLocation().getBlockY();
				int bZ = shop.getLocation().getBlockZ();
				if(bX>=minX && bX<=maxX) {
					if(bY>=minY && bY<=maxY) {
						if(bZ>=minZ && bZ<=maxZ) {
							//In region, we need remove that shop.
							Util.debugLog("Removed shop at:["+shop.getLocation().toString()+"] cause AreaShop region unrented!");
							shop.delete();
						}
					}
				}
			}
				
			
		}
	}
}
