/*
 * This file is a part of project QuickShop, the name is InternalListener.java
 * Copyright (C) Ghost_chu <https://github.com/Ghost-chu>
 * Copyright (C) Bukkit Commons Studio and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.maxgamer.quickshop.InternalListener;

import lombok.AllArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.maxgamer.quickshop.Event.*;
import org.maxgamer.quickshop.Listeners.ListenerHelper;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.Shop.Shop;
import org.maxgamer.quickshop.Shop.ShopType;

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
        if ((event.getShop().getShopType()) == ShopType.BUYING ) {
        	plugin.log("Player " + event.getPlayer().getName() + " sold " + event.getShop().ownerName() + " shop "+event.getShop()+" for items x" + event
            .getAmount() + " for " + plugin.getEconomy().format(event.getBalance()) + " (" + plugin.getEconomy()
            .format(event.getTax()) + " tax).");
        }
        if ((event.getShop().getShopType()) == ShopType.SELLING ) {
        	plugin.log("Player " + event.getPlayer().getName() + " bought " + event.getShop().ownerName() + " shop "+event.getShop()+" for items x" + event
            .getAmount() + " for " + plugin.getEconomy().format(event.getBalance()) + " (" + plugin.getEconomy()
            .format(event.getTax()) + " tax).");
        }
        
      
    }

}
