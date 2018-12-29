package org.maxgamer.quickshop.Listeners;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import org.maxgamer.quickshop.Util.*;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Database.DatabaseHelper;
import org.maxgamer.quickshop.Shop.Shop;
import me.wiefferink.areashop.events.notify.UnrentedRegionEvent;

public class AreaShopListener implements Listener {
	QuickShop plugin = QuickShop.instance;
	
	@EventHandler
	public void unRentedArea(UnrentedRegionEvent e) {
		Vector areaMaxVector = null;
		Vector areaMinVector = null;
		int minX = 0;
		int maxX= 0;
		int minY = 0;
		int maxY = 0;
		int minZ = 0;
		int maxZ = 0;
		try {
		areaMaxVector = e.getRegion().getMaximumPoint();
		areaMinVector = e.getRegion().getMinimumPoint();
		minX = areaMinVector.getBlockX();
		maxX = areaMaxVector.getBlockX();
		minY = areaMinVector.getBlockY();
		maxY = areaMaxVector.getBlockY();
		minZ = areaMinVector.getBlockZ();
		maxZ = areaMaxVector.getBlockZ();
		}catch (Exception ex) {
			plugin.getLogger().warning("You are using not incompatible AreaShop, this feature will cannot working!!!");
			plugin.getLogger().warning("Please use 2.5.0#271 or higher build!");
			plugin.getLogger().warning("You can download our recommend AreaShop build at there: https://github.com/Ghost-chu/QuickShop-Reremake/raw/master/lib/AreaShop.jar");
			return;
		}
		Iterator<Shop> shops = plugin.getShopManager().getShopIterator();
		while (shops.hasNext()) {
			Shop shop = shops.next();
			java.util.List<Shop> waitingRemove = new ArrayList<Shop>();
			if(shop.getLocation().getWorld().getName()==e.getRegion().getWorld().getName()) {
				int bX = shop.getLocation().getBlockX();
				int bY = shop.getLocation().getBlockY();
				int bZ = shop.getLocation().getBlockZ();
				if(bX>=minX && bX<=maxX) {
					if(bY>=minY && bY<=maxY) {
						if(bZ>=minZ && bZ<=maxZ) {
							//In region, we need remove that shop.
							waitingRemove.add(shop);
						}
					}
				}
			}
			for (Shop removeShop : waitingRemove) {
				Util.debugLog("Removed shop at:["+shop.getLocation().toString()+"] cause AreaShop region unrented!");
				removeShop.delete();
				try {
					DatabaseHelper.removeShop(plugin.getDB(), shop.getLocation().getBlockX(), shop.getLocation().getBlockY(), shop.getLocation().getBlockZ(), shop.getLocation().getWorld().getName());
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
			}
				
			
		}
	}
}
