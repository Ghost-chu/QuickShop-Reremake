package org.maxgamer.quickshop.InternalListener;

import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.Event.*;
import org.maxgamer.quickshop.Listeners.ListenerHelper;
import org.maxgamer.quickshop.QuickShop;

@AllArgsConstructor
public class InternalListener implements Listener {
    private QuickShop plugin;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopCreate(ShopCreateEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        plugin.log("Player " + event.getPlayer().getName() + " created a shop at location " + event.getShop().getLocation());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopDelete(ShopDeleteEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        plugin.log("Shop at " + event.getShop().getLocation() + " was removed.");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopModeratorChanges(ShopModeratorChangedEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        plugin.log("Shop at location " + event.getShop().getLocation() + " moderator was changed to " + event.getModerator());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopPriceChanges(ShopPriceChangeEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        plugin.log("Shop at location " + event.getShop().getLocation() + " price was changed from " + event
                .getOldPrice() + " to " + event.getNewPrice());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void shopPurchase(ShopSuccessPurchaseEvent event) {
        if (ListenerHelper.isDisabled(event.getClass())) {
            return;
        }
        plugin.log("Player " + event.getPlayer().getName() + " purchased " + event.getShop().ownerName() + " shop "+event.getShop()+" for items x" + event
                .getAmount() + " for " + plugin.getEconomy().format(event.getBalance()) + " (" + plugin.getEconomy()
                .format(event.getTax()) + " tax).");
    }

}
