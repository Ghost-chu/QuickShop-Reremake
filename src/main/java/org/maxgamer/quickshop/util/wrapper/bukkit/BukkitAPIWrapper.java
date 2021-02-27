/*
 * This file is a part of project QuickShop, the name is BukkitAPIWrapper.java
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

package org.maxgamer.quickshop.util.wrapper.bukkit;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface BukkitAPIWrapper {
    void teleportEntity(
            @NotNull Entity entity,
            @NotNull Location location,
            @Nullable PlayerTeleportEvent.TeleportCause cause);

    void getChunkAt(
            @NotNull World world,
            @NotNull Location location,
            @NotNull CompletableFuture<Chunk> futureTask);

    void getChunkAt(@NotNull World world, int x, int z, @NotNull CompletableFuture<Chunk> futureTask);

    void getChunkAt(
            @NotNull World world, @NotNull Block block, @NotNull CompletableFuture<Chunk> futureTask);

}
