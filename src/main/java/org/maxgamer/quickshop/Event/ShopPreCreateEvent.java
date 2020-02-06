/*
 * This file is a part of project QuickShop, the name is ShopPreCreateEvent.java
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
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

/**
 * This event is called before the shop creation request is sent. E.g. A player clicks a chest, this
 * event is thrown, if successful, the player is asked how much they wish to trade for.
 */
public class ShopPreCreateEvent extends QSEvent implements Cancellable {

  @Getter @NotNull private final Location location;

  @Getter @NotNull private final Player player;

  private boolean cancelled;

  /**
   * Calling when shop pre-creating. Shop won't one-percent will create after this event, if you
   * want get the shop created event, please use ShopCreateEvent
   *
   * @param player Target player
   * @param location The location will create be shop
   */
  public ShopPreCreateEvent(@NotNull Player player, @NotNull Location location) {
    this.location = location;
    this.player = player;
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  @Override
  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }
}
