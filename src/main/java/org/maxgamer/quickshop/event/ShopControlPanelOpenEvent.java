/*
 * This file is a part of project QuickShop, the name is ShopControlPanelOpenEvent.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.event;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.maxgamer.quickshop.shop.Shop;

public class ShopControlPanelOpenEvent extends QSEvent implements Cancellable {
    @Getter
    private final Shop shop;
    @Getter
    private final CommandSender sender;
    private boolean cancelled = false;

    /**
     * Called before shop control panel message.
     *
     * @param shop   The shop bought from
     * @param sender The player which receiving shop control panel message
     */
    public ShopControlPanelOpenEvent(@NotNull Shop shop, @NotNull CommandSender sender) {
        this.shop = shop;
        this.sender = sender;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }
}
