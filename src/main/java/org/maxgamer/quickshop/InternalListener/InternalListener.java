package org.maxgamer.quickshop.InternalListener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.Event.*;
import org.maxgamer.quickshop.QuickShop;

public class InternalListener implements Listener {
    private QuickShop plugin = QuickShop.instance;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopCreate(ShopCreateEvent event) {
        plugin.log("Player " + event.getPlayer().getName() + " created a shop at location " + event.getShop().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopDelete(ShopDeleteEvent event) {
        plugin.log("Shop at " + event.getShop().getLocation() + " was removed.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopModeratorChanges(ShopModeratorChangedEvent event) {
        plugin.log("Shop at location " + event.getShop().getLocation() + " moderator was changed to " + event.getModerator()
                .toString());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopPriceChanges(ShopPriceChangeEvent event) {
        plugin.log("Shop at location " + event.getShop().getLocation() + " price was changed from " + event
                .getOldPrice() + " to " + event.getNewPrice());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopPurchase(ShopSuccessPurchaseEvent event) {
        plugin.log("Player " + event.getPlayer().getName() + " purchased " + event.getShop().ownerName() + " shop item x" + event
                .getAmount() + " for " + plugin.getEconomy().format(event.getBalance()) + " (" + plugin.getEconomy()
                .format(event.getTax()) + " tax).");
    }

}
