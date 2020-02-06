/*
 * This file is a part of project QuickShop, the name is PaperWrapper.java
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

package org.maxgamer.quickshop.Util.ServerForkWrapper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PaperWrapper implements BukkitAPIWrapper {
  @Override
  public void teleportEntity(
      @NotNull Entity entity,
      @NotNull Location location,
      @Nullable PlayerTeleportEvent.TeleportCause cause) {
    if (cause == null) {
      entity.teleportAsync(location);
    } else {
      entity.teleportAsync(location, cause);
    }
  }

  @Override
  public void getChunkAt(
      @NotNull World world,
      @NotNull Location location,
      @NotNull CompletableFuture<Chunk> futureTask) {
    try {
      futureTask.complete(world.getChunkAtAsync(location).get());
    } catch (InterruptedException | ExecutionException e) {
      futureTask.complete(world.getChunkAt(location));
    }
  }

  @Override
  public void getChunkAt(
      @NotNull World world, int x, int z, @NotNull CompletableFuture<Chunk> futureTask) {
    try {
      futureTask.complete(world.getChunkAtAsync(x, z).get());
    } catch (InterruptedException | ExecutionException e) {
      futureTask.complete(world.getChunkAt(x, z));
    }
  }

  @Override
  public void getChunkAt(
      @NotNull World world, @NotNull Block block, @NotNull CompletableFuture<Chunk> futureTask) {
    try {
      futureTask.complete(world.getChunkAtAsync(block).get());
    } catch (InterruptedException | ExecutionException e) {
      futureTask.complete(world.getChunkAt(block));
    }
  }
}
