/*
 * This file is a part of project QuickShop, the name is ShopCreateEvent.java
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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.shop.Shop;

import java.util.UUID;

public class ShopCreateEvent extends QSEvent implements Cancellable {

    @Getter
    @NotNull
    private final UUID creator;

    @Getter
    @Nullable
    @Deprecated
    private final Player player;

    @Getter
    @NotNull
    private final Shop shop;

    private boolean cancelled;

    /**
     * Call when have a new shop was creating.
     *
     * @param shop    Target shop
     * @param creator The player creating the shop, the player might offline/not exist if creating by a plugin.
     */
    public ShopCreateEvent(@NotNull Shop shop, @NotNull UUID creator) {
        this.shop = shop;
        this.creator = creator;
        this.player = Bukkit.getPlayer(creator);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
