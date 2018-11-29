package org.maxgamer.quickshop.Shop;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;

public class DisplayItemStorage {
	static Map<UUID,Shop> id2ShopMap = new HashMap<>();
	public Shop getDisplayItemShop(UUID uuid) {
		return id2ShopMap.get(uuid);
	}
	public static void addDisplayItemShop(UUID uuid,Shop shop) {
		id2ShopMap.put(uuid, shop);
	}
	public static Location getDisplayLocation(UUID uuid) {
		Shop shop = id2ShopMap.get(uuid);
		if(shop==null)
			return null;
		return shop.getLocation().clone().add(0.5, 1.2, 0.5);
	}
	public static UUID getShopDisplayItem(Shop shop) {
		Set<Entry<UUID,Shop>> set=id2ShopMap.entrySet();  
		Iterator<Entry<UUID, Shop>>  it=set.iterator();  
		while(it.hasNext()) {  
		   Entry<UUID, Shop> entry =(Entry<UUID, Shop>)it.next();  
		   if(entry.getValue().equals(shop)) {
			   return entry.getKey();
		   }
		} 
		return null;
	}

	public static void removeDisplayItemByShop(Shop shop) {
		try {
			Set<Entry<UUID, Shop>> set = id2ShopMap.entrySet();
			Iterator<Entry<UUID, Shop>> it = set.iterator();
			while (it.hasNext()) {
				Entry<UUID, Shop> entry = (Entry<UUID, Shop>) it.next();
				if (entry.getValue().equals(shop)) {
					id2ShopMap.remove(entry.getKey());
				}
			}
		} catch (Exception e) {

		}
	}
	public static void removeDisplayItemByUUID(UUID uuid) {
		try {
		id2ShopMap.remove(uuid);
		}catch (Exception e) {
			
		}
	}
}
