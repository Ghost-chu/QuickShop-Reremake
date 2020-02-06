/*
 * This file is a part of project QuickShop, the name is ShopProtectionCheckEvent.java
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

package org.maxgamer.quickshop.Event;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

public class ShopProtectionCheckEvent extends QSEvent {

    @Getter
    @NotNull
    private final Player player;

    @Getter
    @NotNull
    private final Location loc;

    @NotNull
    private final Event event; // Don't use getter, we have important notice need told dev in javadoc.

    @Getter
    @NotNull
    private final ProtectionCheckStatus status;

    /**
     * Will call when shop price was changed.
     *
     * @param location Target location will execute protect check.
     * @param status   The checking status
     * @param event    The event will call to check the permissions.
     * @param player   The player in was mentions in this event
     */
    public ShopProtectionCheckEvent(
            @NotNull Location location,
            @NotNull Player player,
            @NotNull ProtectionCheckStatus status,
            @NotNull Event event) {
        this.loc = location;
        this.player = player;
        this.status = status;
        this.event = event;
    }

    /**
     * Get the event will used for permission check. WARN: This might not only BlockBreakEvent, you
     * should check the event type before casting.
     *
     * @return The protection check event.
     */
    @NotNull
    public Event getEvent() {
        return event;
    }
}
