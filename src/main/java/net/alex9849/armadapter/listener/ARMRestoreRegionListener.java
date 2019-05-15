package net.alex9849.armadapter.listener;

import net.alex9849.arm.events.ResetBlocksEvent;
import net.alex9849.inter.WGRegion;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopChunk;

import java.util.HashMap;

public class ARMRestoreRegionListener implements Listener {


    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleRegionReset(ResetBlocksEvent event) {
        if(event.isCancelled()) {
            return;
        }

        //Gets an API friendly WorldGuard region object
        WGRegion wgRegion = event.getRegion().getRegion();

        HashMap<ShopChunk, HashMap<Location, Shop>> worldShops = QuickShop.instance.getShopManager().getShops(event.getRegion().getRegionworld().getName());

        //For all shops that are in the world of my region...
        for(HashMap<Location, Shop> locationShopHashMap : worldShops.values()) {

            for(Location shopLocaltion : locationShopHashMap.keySet()) {

                //...check if the shop is inside the region that should be resetted...
                if(wgRegion.contains(shopLocaltion.getBlockX(), shopLocaltion.getBlockY(), shopLocaltion.getBlockZ())) {

                    //If yes get the shop
                    Shop deleteShop = locationShopHashMap.get(shopLocaltion);

                    //Check if the shop really exists...
                    if(deleteShop != null) {
                        //...and delete the shop
                        deleteShop.delete(false);
                    }

                }

            }

        }


    }
}
